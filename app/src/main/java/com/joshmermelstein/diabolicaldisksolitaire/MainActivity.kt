package com.joshmermelstein.diabolicaldisksolitaire

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    // Because I'm dumb, I have to redraw the entire UI on reload to pick up new star
    // number/completion numbers. This set keeps track of which expandos are expanded so I can
    // reopen them and make the UI look more continuous.
    private val expandedListItems = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.pack_select_toolbar))
        redrawLevelSelect()
        findViewById<ScrollView>(R.id.PackScrollView).isSmoothScrollingEnabled = true
    }

    // Because I am dumb and didn't use Room or something for my underlying storage, I need to
    // explicitly redraw all the buttons after a level completes in order to update the displayed
    // stars.
    override fun onResume() {
        redrawLevelSelect()
        super.onResume()
    }

    private fun redrawLevelSelect() {
        findViewById<LinearLayout>(R.id.PackLinearLayout).removeAllViews()

        // Info on which levels exist and how to group them is lazily loaded into a global
        // singleton for easy lookup.
        MetadataSingleton.getInstance(this).packData.keys.forEach {
            appendLevelPack(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.level_select_dropdown, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.resetAll -> {
                resetAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun makeHeader(packId: String): LinearLayout {
        // Create a layout row for views related to this pack.
        val header = LinearLayout(this)
        header.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Write the name of the pack
        TextView(this).also {
            it.text = packId
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            it.setPadding(0, 0, 0, 25)
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1F
            )
            header.addView(it)
        }

        // Write how much of the pack the user has beaten.
        TextView(this).also {
            it.text = MetadataSingleton.getInstance(this@MainActivity).getNumComplete(packId)
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            header.addView(it)
        }

        // configure onclick UI effect
        addOnclickEffect(header)

        return header
    }

    private fun makeLevelButtons(packId: String, levels: List<String>): LinearLayout {
        val numCols = 4
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val metadata = MetadataSingleton.getInstance(this)

        var levelsInRow = 0
        var row = LinearLayout(this)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        for (id in levels) {
            val levelData = metadata.getLevelData(id) ?: continue
            val btnTag = Button(this)
            btnTag.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            btnTag.text = levelData.displayId
            btnTag.backgroundTintList =
                ColorStateList.valueOf(buttonColor(bestScore(levelData.canonicalId)))
            btnTag.setOnClickListener {
                val intent = Intent(this, GameplayActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            row.addView(btnTag)

            levelsInRow++
            if (levelsInRow % numCols == 0) {
                buttonContainer.addView(row)
                row = LinearLayout(this)
                row.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                levelsInRow = 0
            }
        }

        // To keep all boxes the same width, we add a few extra invisible boxes to the last row if
        // it wasn't already full.
        if (levelsInRow % numCols != 0) {
            repeat(numCols - levelsInRow) {
                val btnTag = Button(this)
                btnTag.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                btnTag.visibility = View.INVISIBLE
                btnTag.text = "\n"
                row.addView(btnTag)
            }
            buttonContainer.addView(row)
        }
        if (!expandedListItems.contains(packId)) {
            buttonContainer.visibility = View.GONE
        }
        return buttonContainer
    }

    private fun bestScore(id: String): Int {
        val highscores = getSharedPreferences("highscores", Context.MODE_PRIVATE)
        return highscores.getInt(id, Int.MAX_VALUE)
    }

    private fun buttonColor(bestScore: Int): Int {
        return if (bestScore != Int.MAX_VALUE) {
            ContextCompat.getColor(this, R.color.completed_level)
        } else {
            ContextCompat.getColor(this, R.color.incomplete_level)
        }
    }

    // Helper for adding a group of levels to the level select screen.
    private fun appendLevelPack(packId: String) {
        // Ensure we have data to lay out.
        val levels = MetadataSingleton.getInstance(this).packData[packId] ?: return

        // Create a list item to hold views related to this level pack
        val layout = findViewById<LinearLayout>(R.id.PackLinearLayout)


        val header = makeHeader(packId)
        val levelsContainer = makeLevelButtons(packId, levels)

        // Configure onclick listener to move to Level Select activity for this pack ID.
        header.setOnClickListener {
            levelsContainer.visibility =
                if (levelsContainer.visibility == View.GONE) {
                    expandedListItems.add(packId)
                    View.VISIBLE
                } else {
                    expandedListItems.remove(packId)
                    View.GONE
                }
            // Focus the second row of the expanded pack. This scrolls a bit if the pack was at the
            // bottom of the screen but not a jarring amount of the container being expanded is
            // larger than the screen.
            layout.requestChildFocus(
                header,
                levelsContainer.getChildAt((levelsContainer.childCount - 1).coerceAtMost(1))
            )
        }

        // Put header and buttons in a vertical linear layout and add that to the activity's layout.
        LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL
            it.addView(header)
            it.addView(levelsContainer)
            layout.addView(it)
        }
        addDivider(layout)
    }

    // Add a horizontal line between rows of a linear layout for visual clarity.
    private fun addDivider(layout: LinearLayout) {
        val line = View(this).also {
            it.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
            )
            it.setBackgroundColor(getColor(R.color.fixed_disk))
        }
        layout.addView(line)
    }

    private fun addOnclickEffect(v: View) {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray: TypedArray = obtainStyledAttributes(attrs)
        val backgroundResource = typedArray.getResourceId(0, 0)
        v.setBackgroundResource(backgroundResource)
        typedArray.recycle()
    }

    // A dialog to make sure the user really wants to delete all their saved data.
    private fun resetAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset")
            .setMessage("Are you sure? This will reset progress on all levels and reset all stars.")
            .setPositiveButton("Yes (delete it all)") { _, _ ->
                clearSharedPreferences()
                deleteSaves()
                redrawLevelSelect()
            }
            .setNegativeButton("no", null)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .show()
    }

    // Deletes all shared preferences which is where highscores are stored.
    private fun clearSharedPreferences() {
        val dir = this.filesDir.parent ?: return
        val subdir = File("$dir/shared_prefs/")
        val children: Array<String> = subdir.list() ?: return
        for (i in children.indices) {
            // clear each preference file
            this.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit()
                .clear().apply()
            //delete the file
            File(subdir, children[i]).delete()
        }
    }

    // Deletes all per-level save files.
    private fun deleteSaves() {
        val dir: File = this.filesDir
        val files: Array<File> = dir.listFiles() ?: return
        for (file in files) {
            file.delete()
        }
    }
}
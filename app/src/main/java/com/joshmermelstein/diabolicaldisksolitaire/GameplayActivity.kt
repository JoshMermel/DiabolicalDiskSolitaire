package com.joshmermelstein.diabolicaldisksolitaire

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

// An activity for displaying the lifetime of a level as well as other UI elements.
class GameplayActivity : AppCompatActivity() {
    // The id is not known until it is read from intent in OnCreate
    // This is the canonical ID of the level (i.e. "a_caged_flip"); not the display ID (i.e. "A10")
    private lateinit var id: String

    // The gameManager is created based on the id so it must be lateInit as well.
    private lateinit var gameManager: GameManager

    // Shared state with the game manager
    private var buttonState = ButtonState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)

        // The normal way of creating a level - looking up its params based on an id
        this.id = intent.getStringExtra("id") ?: return
        val params = loadInitialLevel(id, this)
        if (params == null) {
            finish()
        } else {
            createFromParams(params)
        }
    }

    // Finishes initializing the gameplay activity based on a gameplay params
    private fun createFromParams(params: GameplayParams) {
        this.gameManager = GameManager(params, this, buttonState)

        // TODO(jmerm): set tutorial text here

        findViewById<GameplayView>(R.id.gameplayView).gameManager = this.gameManager
        findViewById<Toolbar>(R.id.gameplay_toolbar).also {
            setSupportActionBar(it)
        }

        supportActionBar?.title = params.id

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gameplay_dropdown, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.toolbar_undo)?.isEnabled = buttonState.undoButtonEnabled
        menu?.findItem(R.id.toolbar_redo)?.isEnabled = buttonState.redoButtonEnabled
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset -> {
                gameManager.reset()
                true
            }
            R.id.toolbar_undo -> {
                gameManager.undoMove()
                true
            }
            R.id.toolbar_redo -> {
                gameManager.redoMove()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// The gameplay manager needs to control some state on this activity. Rather than letting it
// manipulate state directly, we use a reference to one of these structs to pass shared state.
class ButtonState {
    var undoButtonEnabled = true
    var redoButtonEnabled = true
}
package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Fill in most text boxes with hardcoded strings
        hashMapOf(
            R.id.about_game to R.string.aboutGameText,
            R.id.github to R.string.githubText,
            // TODO(jmerm): note inspirations
            // TODO(jmerm): link to other game
        ).forEach {
            initTextView(it.key, it.value)
        }

        // Magic to make a substring be a link that opens an intent
        val librariesText =
            getString(R.string.openSourceLibrariesText).toSpannable().apply {
                this[28..49] = object : ClickableSpan() {
                    override fun onClick(view: View) {
                        startActivity(
                            Intent(
                                this@AboutActivity,
                                OssLicensesMenuActivity::class.java
                            )
                        )
                    }
                }
            }
        findViewById<TextView>(R.id.libraries).apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = librariesText
        }
    }

    private fun initTextView(id: Int, textId: Int) {
        findViewById<TextView>(id).apply {
            text = HtmlCompat.fromHtml(
                getString(textId),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
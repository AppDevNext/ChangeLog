package info.hannes.changelog.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import info.hannes.changelog.ChangeLog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val changeLog = ChangeLog(this)
        if (changeLog.isFirstRun) {
            changeLog.logDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_whats_new -> {
                DarkThemeChangeLog(this).logDialog.show()
            }
            R.id.menu_full_changelog -> {
                ChangeLog(this).fullLogDialog.show()
            }
            R.id.menu_logcat -> {
                startActivity(Intent(this, LogcatActivity::class.java))
            }
        }

        return true
    }

    /**
     * Example that shows how to create a themed dialog.
     */
    class DarkThemeChangeLog internal constructor(context: Context) : ChangeLog(ContextThemeWrapper(context, R.style.DarkTheme), DARK_THEME_CSS) {
        companion object {
            internal val DARK_THEME_CSS = "body { color: #ffffff; background-color: #282828; }" + "\n" + ChangeLog.DEFAULT_CSS
        }
    }
}

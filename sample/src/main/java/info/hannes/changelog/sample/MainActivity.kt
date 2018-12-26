package info.hannes.changelog.sample

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import de.cketti.sample.changelog.R
import info.hannes.changelog.ChangeLog

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cl = ChangeLog(this)
        if (cl.isFirstRun) {
            cl.logDialog.show()
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

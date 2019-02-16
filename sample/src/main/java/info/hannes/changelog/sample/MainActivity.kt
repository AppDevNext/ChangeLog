package info.hannes.changelog.sample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import info.hannes.changelog.ChangeLog
import info.hannes.changelog.sample.iabutils.IabHelper
import info.hannes.changelog.sample.iabutils.IabResult
import info.hannes.changelog.sample.iabutils.IabUtil

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout

    var helper: IabHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // enable ActionBar app icon to behave as action to toggle nav drawer
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
        }

        val changeLog = ChangeLog(this)
        if (changeLog.isFirstRun) {
            changeLog.logDialog.show()
        }

        //payment

        val base64EncodedPublicKey = IabUtil.key

        // compute your public key and store it in base64EncodedPublicKey
        helper = IabHelper(this, base64EncodedPublicKey)
        helper?.enableDebugLogging(true)

        helper?.startSetup(object : IabHelper.OnIabSetupFinishedListener {
            override fun onIabSetupFinished(result: IabResult) {
                if (!result.isSuccess) {
                    // Oh noes, there was a problem.
                    Log.d("Payment", "Problem setting up In-app Billing: $result")
                    return
                }

                // Hooray, IAB is fully set up!
                IabUtil.getInstance().retrieveData(helper)
            }
        })

    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            selectNavigationItem(menuItem.itemId)
            mDrawerLayout.closeDrawers()
            true
        }

    }

    private fun selectNavigationItem(itemId: Int) {

        when (itemId) {
            R.id.nav_full_changelog -> ChangeLog(this).fullLogDialog.show()
            R.id.nav_whats_new -> DarkThemeChangeLog(this).logDialog.show()
            R.id.nav_logcat -> startActivity(Intent(this, LogcatActivity::class.java))
            R.id.nav_other_donate -> IabUtil.showBeer(this, helper)
            R.id.nav_other_github -> {
                val url = "https://github.com/hannesa2/ChangeLog"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.drawer_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.nav_whats_new -> {
                DarkThemeChangeLog(this).logDialog.show()
            }
            R.id.nav_full_changelog -> {
                ChangeLog(this).fullLogDialog.show()
            }
            R.id.nav_logcat -> {
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

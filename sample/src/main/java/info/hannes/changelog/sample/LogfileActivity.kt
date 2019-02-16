package info.hannes.changelog.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import info.hannes.logcat.LogfileFragment
import info.hannes.timber.FileLoggingTree


class LogfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        // Check that the activity is using the layout version with the fragment_container FrameLayout
        if (findViewById<View>(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return
            }

            // Create a new Fragment to be placed in the activity layout
            val firstFragment = LogfileFragment.newInstance(FileLoggingTree.getFilername(), "logfile.log")

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            // firstFragment.arguments = intent.extras

            // Add the fragment to the 'fragment_container' FrameLayout
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit()
        }

    }

}

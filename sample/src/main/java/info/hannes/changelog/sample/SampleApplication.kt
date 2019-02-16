package info.hannes.changelog.sample

import android.app.Application
import info.hannes.timber.FileLoggingTree
import timber.log.Timber


class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(FileLoggingTree(externalCacheDir))
        Timber.d("Debug test")
        Timber.w("Warning test")
        Timber.e("Error test")
    }
}
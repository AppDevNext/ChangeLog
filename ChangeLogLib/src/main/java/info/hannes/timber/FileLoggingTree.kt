package info.hannes.timber

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class FileLoggingTree(externalCacheDir: File?) : Timber.DebugTree() {

    init {
        val fileNameTimeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        FILE = File(externalCacheDir, "$fileNameTimeStamp.log")
    }

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val logTimeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(Date())

            val prio = when (priority) {
                2 -> "V:"
                3 -> "D:"
                4 -> "I:"
                5 -> "W:"
                6 -> "E:"
                7 -> "A:"
                else -> priority.toString()
            }

            val writer = FileWriter(FILE, true)
            writer.append(prio)
                    .append(" ")
                    .append(logTimeStamp)
                    .append(tag)
                    .append(message)
                    .append("\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error while logging into file : $e")
        }

        super.log(priority, tag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String? {

        return String.format(" %s.%s:%s ",
                super.createStackElementTag(element),
                element.methodName,
                element.lineNumber
        )
    }

    companion object {

        private val LOG_TAG = FileLoggingTree::class.java.simpleName
        private lateinit var FILE: File
        fun getFilername(): String = FILE.absolutePath
    }
}
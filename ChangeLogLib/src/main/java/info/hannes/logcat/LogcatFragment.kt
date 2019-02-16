package info.hannes.logcat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import info.hannes.logcat.base.LogBaseFragment
import java.io.IOException
import java.util.*


class LogcatFragment : LogBaseFragment() {

    @SuppressLint("LogNotTimber")
    override fun readLogFile(): ArrayList<String> {
        val logList = ArrayList<String>()
        try {
            val process = Runtime.getRuntime().exec("logcat -dv time")

            logList.addAll(
                    process.inputStream.bufferedReader().use {
                        it.readLines().map { line ->
                            line.replace(" W/", " W: ")
                                    .replace(" E/", " E: ")
                                    .replace(" V/", " V: ")
                                    .replace(" I/", " I: ")
                                    .replace(" D/", " D: ")
                        }
                    }
            )

        } catch (e: IOException) {
            Log.e("LoadingLogcatTask", e.message)
        }

        return logList
    }

    companion object {
        fun newInstance(targetFileName: String): LogcatFragment {
            val fragment = LogcatFragment()
            val args = Bundle()
            args.putString(FILE_NAME, targetFileName)
            fragment.arguments = args
            return fragment
        }

    }
}
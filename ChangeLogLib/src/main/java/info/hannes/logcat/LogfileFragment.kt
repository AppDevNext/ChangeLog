package info.hannes.logcat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.hannes.logcat.base.LogBaseFragment
import java.io.File
import java.util.*


class LogfileFragment : LogBaseFragment() {

    private lateinit var sourceFileName: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sourceFileName = arguments?.getString(SOURCE_FILE_NAME)!!

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun readLogFile(): ArrayList<String> {
        return File(sourceFileName).useLines { ArrayList(it.toList()) }
    }

    companion object {
        fun newInstance(sourceFileName: String, targetFileName: String): LogfileFragment {
            val fragment = LogfileFragment()
            val args = Bundle()
            args.putString(SOURCE_FILE_NAME, sourceFileName)
            args.putString(FILE_NAME, targetFileName)
            fragment.arguments = args
            return fragment
        }

        private const val SOURCE_FILE_NAME = "sourceFileName"
    }
}
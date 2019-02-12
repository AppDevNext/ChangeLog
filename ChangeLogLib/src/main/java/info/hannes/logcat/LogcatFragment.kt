package info.hannes.logcat

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.util.*


class LogcatFragment : Fragment() {

    private var logsRecycler: RecyclerView? = null
    private var logListAdapter: LogListAdapter? = null
    private var searchView: SearchView? = null
    private val currentFilter = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(info.hannes.R.layout.fragment_logcat, container, false)

        val layoutManager = LinearLayoutManager(context)
        logsRecycler = view.findViewById(info.hannes.R.id.log_recycler)
        logsRecycler!!.setHasFixedSize(true)
        logsRecycler!!.layoutManager = layoutManager

        if (activity!!.actionBar != null) {
            activity!!.actionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            showLoadingDialog()

            val task = LoadingLogcatTask()
            task.execute()
        }

        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(info.hannes.R.menu.menu_log, menu)
        val searchItem = menu!!.findItem(info.hannes.R.id.menu_search)
        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            // searchView.setQueryHint(getString(android.R.string.search_hint));
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        }

        if (searchView == null) {
            return
        }
        val searchAutoComplete = searchView!!.findViewById<SearchView.SearchAutoComplete>(info.hannes.R.id.search_src_text)

        if (searchView != null && currentFilter != "") {
            searchAutoComplete?.setText(currentFilter)
            searchView!!.isIconified = false
        } else {
            searchAutoComplete?.setText("")
            searchView!!.isIconified = true
        }

        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                setFilter2LogAdapter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                setFilter2LogAdapter(query)
                return true
            }
        }
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                setFilter2LogAdapter("")
                return true  // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Do something when expanded
                return true  // Return true to expand action view
            }
        })

        if (null != searchView) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            searchView!!.setIconifiedByDefault(true)
            searchView!!.setOnQueryTextListener(queryTextListener)
            if (currentFilter != "") {
                if (searchAutoComplete != null && searchItem != null) {
                    searchItem.expandActionView()
                    searchAutoComplete.setText(currentFilter)
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setFilter2LogAdapter(filter: String) {
        logListAdapter!!.setFilter(filter)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var returnValue = true
        val i = item!!.itemId
        if (i == info.hannes.R.id.menu_share) {
            sendLogcat(logListAdapter!!.filterLogs)
        } else {
            returnValue = super.onOptionsItemSelected(item)
        }
        return returnValue
    }

    private fun sendLogcat(filterLogs: List<String>) {

        var emailAddress: String
        try {
            val stringClass = info.hannes.R.string::class.java
            val mailLoggerField = stringClass.getField("mail_logger")
            val emailAddressId = mailLoggerField.get(null) as Int
            emailAddress = getString(emailAddressId)
        } catch (e: Exception) {
            emailAddress = ""
        }

        val logcatFile = File(this@LogcatFragment.activity?.externalCacheDir, "logcat.log")
        logcatFile.writeText(filterLogs.joinToString("\n"))

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/zip"

        val uri = Uri.fromFile(logcatFile)
        intent.putExtra(Intent.EXTRA_STREAM, uri)


        intent.putExtra(Intent.EXTRA_EMAIL, emailAddress)
        val subject = String.format(getString(info.hannes.R.string.log_send_mail_subject), getString(info.hannes.R.string.app_name))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = MAIL_ATTACHMENT_TYPE
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            // prevent from a "exposed beyond app through ClipData.Item.getUri()"
            // https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())

            startActivity(Intent.createChooser(intent, "Logcat ..."))
        } catch (e: ActivityNotFoundException) {
            val snackbar = Snackbar.make(
                    this@LogcatFragment.activity!!.findViewById<View>(android.R.id.content),
                    info.hannes.R.string.log_send_no_app,
                    Snackbar.LENGTH_LONG
            )
            snackbar.show()
        }

    }

    /**
     * Class for loading the log data async
     */
    private inner class LoadingLogcatTask : AsyncTask<String, Void, ArrayList<String>>() {

        override fun doInBackground(vararg args: String): ArrayList<String> {
            return readLogFile()
        }

        override fun onPostExecute(result: ArrayList<String>?) {
            if (result != null) {
                logListAdapter = LogListAdapter(result, currentFilter)
                logsRecycler!!.adapter = logListAdapter
                logsRecycler!!.scrollToPosition(result.size - 1)
                dismissLoadingDialog()
            }
        }

        /**
         * Read and show log file info
         */
        private fun readLogFile(): ArrayList<String> {
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
    }

    fun showLoadingDialog() {
        val loading = LoadingDialog.newInstance(false)
        val fm = this@LogcatFragment.activity!!.supportFragmentManager
        val ft = fm.beginTransaction()
        loading.show(ft, DIALOG_WAIT_TAG)
    }

    fun dismissLoadingDialog() {
        val frag = this@LogcatFragment.activity!!.supportFragmentManager.findFragmentByTag(DIALOG_WAIT_TAG)
        if (frag != null) {
            val loading = frag as LoadingDialog?
            loading!!.dismiss()
        }
    }

    companion object {

        private const val MAIL_ATTACHMENT_TYPE = "text/plain"

        private const val DIALOG_WAIT_TAG = "DIALOG_WAIT"
    }

}
package info.hannes.logcat.base

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import info.hannes.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*


abstract class LogBaseFragment : Fragment() {

    private lateinit var logsRecycler: RecyclerView
    private var logListAdapter: LogListAdapter? = null
    private var searchView: SearchView? = null
    private val currentFilter = ""

    private var filename: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(info.hannes.R.layout.fragment_log, container, false)

        val layoutManager = LinearLayoutManager(context)
        logsRecycler = view.findViewById(R.id.log_recycler)
        logsRecycler.setHasFixedSize(true)
        logsRecycler.layoutManager = layoutManager

        if (activity!!.actionBar != null) {
            activity!!.actionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            showLogContent()
        }

        setHasOptionsMenu(true)

        filename = arguments?.getString(FILE_NAME)

        return view
    }

    private fun showLogContent() = GlobalScope.launch(Dispatchers.Main) {
        showLoadingDialog()
        val logEntries = withContext(Dispatchers.Default) {
            Timber.d("try to read logEntries %s", Thread.currentThread().name)
            readLogFile()
        }
        logListAdapter = LogListAdapter(logEntries, currentFilter)
        logsRecycler.adapter = logListAdapter
        Timber.d("read logEntries %s %s", logEntries.size, Thread.currentThread().name)
        logsRecycler.adapter?.itemCount?.minus(1)?.let { logsRecycler.scrollToPosition(it) }

        dismissLoadingDialog()
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

        searchView?.let {
            it.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            it.setIconifiedByDefault(true)
            it.setOnQueryTextListener(queryTextListener)
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
        logListAdapter?.setFilter(filter)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var returnValue = true
        val i = item!!.itemId
        if (i == info.hannes.R.id.menu_share) {
            filename?.let { sendLogContent(logListAdapter!!.filterLogs, it) }
        } else {
            returnValue = super.onOptionsItemSelected(item)
        }
        return returnValue
    }

    private fun sendLogContent(filterLogs: List<String>, filename: String) {

        val emailAddress: String
        emailAddress = try {
            val stringClass = info.hannes.R.string::class.java
            val mailLoggerField = stringClass.getField("mail_logger")
            val emailAddressId = mailLoggerField.get(null) as Int
            getString(emailAddressId)
        } catch (e: Exception) {
            ""
        }

        val logtoSend = File(this@LogBaseFragment.activity?.externalCacheDir, filename)
        logtoSend.writeText(filterLogs.joinToString("\n"))

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/zip"

        val uri = Uri.fromFile(logtoSend)
        intent.putExtra(Intent.EXTRA_STREAM, uri)


        intent.putExtra(Intent.EXTRA_EMAIL, emailAddress)
        val subject = String.format(filename, getString(info.hannes.R.string.app_name))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = MAIL_ATTACHMENT_TYPE
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            // prevent from a "exposed beyond app through ClipData.Item.getUri()"
            // https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())

            startActivity(Intent.createChooser(intent, "$filename ..."))
        } catch (e: ActivityNotFoundException) {
            val snackbar = Snackbar.make(
                    this@LogBaseFragment.activity!!.findViewById<View>(android.R.id.content),
                    info.hannes.R.string.log_send_no_app,
                    Snackbar.LENGTH_LONG
            )
            snackbar.show()
        }

    }

    abstract fun readLogFile(): ArrayList<String>

    private fun showLoadingDialog() {
        val loading = LoadingDialog.newInstance(false)
        val fm = this@LogBaseFragment.activity!!.supportFragmentManager
        val ft = fm.beginTransaction()
        loading.show(ft, DIALOG_WAIT_TAG)
    }

    private fun dismissLoadingDialog() {
        val frag = this@LogBaseFragment.activity!!.supportFragmentManager.findFragmentByTag(DIALOG_WAIT_TAG)
        if (frag != null) {
            val loading = frag as LoadingDialog?
            loading!!.dismiss()
        }
    }

    companion object {

        private const val MAIL_ATTACHMENT_TYPE = "text/plain"

        private const val DIALOG_WAIT_TAG = "DIALOG_WAIT"

        const val FILE_NAME = "filename"
    }

}
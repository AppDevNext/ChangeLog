package info.hannes.changelog

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.util.SparseArray
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.hannes.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*


/**
 * Display a dialog showing a full or partial (What's New) change log.
 */
open class ChangeLog
/**
 * Create a `ChangeLog` instance using the supplied `SharedPreferences` instance.
 *
 * @param context     Context that is used to access the resources and to create the ChangeLog dialogs.
 * @param preferences `SharedPreferences` instance that is used to persist the last version code.
 * @param css         CSS styles used to format the change log (excluding `<style>` and
 * `</style>`).
 */
@JvmOverloads constructor(
        /**
         * Context that is used to access the resources and to create the ChangeLog dialogs.
         */
        private val context: Context, preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        /**
         * Contains the CSS rules used to format the change log.
         */
        protected val css: String = DEFAULT_CSS) {

    /**
     * Last version code read from `SharedPreferences` or [.NO_VERSION].
     */
    /**
     * Get version code of last installation.
     *
     * @return The version code of the last installation of this app (as described in the former
     * manifest). This will be the same as returned by [.getCurrentVersionCode] the
     * second time this version of the app is launched (more precisely: the second time
     * `ChangeLog` is instantiated).
     * @see [android:versionCode](http://developer.android.com/guide/topics/manifest/manifest-element.html.vcode)
     */
    val lastVersionCode: Int

    /**
     * Version code of the current installation.
     */
    /**
     * Get version code of current installation.
     *
     * @return The version code of this app as described in the manifest.
     * @see [android:versionCode](http://developer.android.com/guide/topics/manifest/manifest-element.html.vcode)
     */
    var currentVersionCode: Int = 0
        private set

    /**
     * Version name of the current installation.
     */
    /**
     * Get version name of current installation.
     *
     * @return The version name of this app as described in the manifest.
     * @see [android:versionName](http://developer.android.com/guide/topics/manifest/manifest-element.html.vname)
     */
    var currentVersionName: String? = null
        private set

    /**
     * Check if this is the first execution of this app version.
     *
     * @return `true` if this version of your app is started the first time.
     */
    val isFirstRun: Boolean
        get() {
            val first = lastVersionCode < currentVersionCode
            updateVersionInPreferences()
            return first
        }

    /**
     * Check if this is a new installation.
     *
     * @return `true` if your app including `ChangeLog` is started the first time ever.
     * Also `true` if your app was uninstalled and installed again.
     */
    val isFirstRunEver: Boolean
        get() {
            val firstEver = lastVersionCode == NO_VERSION
            updateVersionInPreferences()
            return firstEver
        }

    /**
     * Get the "What's New" dialog.
     *
     * @return An AlertDialog displaying the changes since the previous installed version of your
     * app (What's New). But when this is the first run of your app including
     * `ChangeLog` then the full log dialog is show.
     */
    val logDialog: AlertDialog
        get() = getDialog(isFirstRunEver)

    /**
     * Get a dialog with the full change log.
     *
     * @return An AlertDialog with a full change log displayed.
     */
    val fullLogDialog: AlertDialog
        get() = getDialog(true)

    /**
     * Get changes since last version as HTML string.
     *
     * @return HTML string containing the changes since the previous installed version of your app
     * (What's New).
     */
    val log: String
        get() = getLog(false)

    /**
     * Get full change log as HTML string.
     *
     * @return HTML string containing the full change log.
     */
    val fullLog: String
        get() = getLog(true)

    /**
     * Returns a [Comparator] that specifies the sort order of the [ReleaseItem]s.
     *
     *
     *
     *
     * The default implementation returns the items in reverse order (latest version first).
     *
     */
    protected val changeLogComparator: Comparator<ReleaseItem>
        get() = Comparator { lhs, rhs ->
            if (lhs.versionCode < rhs.versionCode) {
                1
            } else if (lhs.versionCode > rhs.versionCode) {
                -1
            } else {
                0
            }
        }

    /**
     * Create a `ChangeLog` instance using the default [SharedPreferences] file.
     *
     * @param context Context that is used to access the resources and to create the ChangeLog dialogs.
     * @param css     CSS styles that will be used to format the change log.
     */
    constructor(context: Context, css: String) : this(context, PreferenceManager.getDefaultSharedPreferences(context), css) {}

    init {

        // Get last version code
        lastVersionCode = preferences.getInt(VERSION_KEY, NO_VERSION)

        // Get current version code and version name
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentVersionCode = packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                currentVersionCode = packageInfo.versionCode
            }
            currentVersionName = packageInfo.versionName
        } catch (e: NameNotFoundException) {
            currentVersionCode = NO_VERSION
            Log.e(LOG_TAG, "Could not get version information from manifest!", e)
        }

    }

    /**
     * Skip the "What's new" dialog for this app version.
     *
     *
     *
     *
     * Future calls to [.isFirstRun] and [.isFirstRunEver] will return `false`
     * for the current app version.
     *
     */
    fun skipLogDialog() {
        updateVersionInPreferences()
    }

    /**
     * Create a dialog containing (parts of the) change log.
     *
     * @param full If this is `true` the full change log is displayed. Otherwise only changes for
     * versions newer than the last version are displayed.
     * @return A dialog containing the (partial) change log.
     */
    private fun getDialog(full: Boolean): AlertDialog {
        val webView = LollipopFixedWebView(context)
        //wv.setBackgroundColor(0); // transparent
        webView.loadDataWithBaseURL(null, getLog(full), "text/html", "UTF-8", null)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(
                context.resources.getString(if (full) R.string.changelog_full_title else R.string.changelog_title))
                .setView(webView)
                .setCancelable(false)
                // OK button
                .setPositiveButton(
                        context.resources.getString(R.string.changelog_ok_button)
                ) { _, _ ->
                    // The user clicked "OK" so save the current version code as "last version code".
                    updateVersionInPreferences()
                }

        if (!full) {
            // Show "More…" button if we're only displaying a partial change log.
            builder.setNegativeButton(R.string.changelog_show_full
            ) { _, _ -> fullLogDialog.show() }
        }

        return builder.create()
    }

    /**
     * Write current version code to the preferences.
     */
    protected fun updateVersionInPreferences() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putInt(VERSION_KEY, currentVersionCode)
        editor.apply()
    }

    /**
     * Get (partial) change log as HTML string.
     *
     * @param full If this is `true` the full change log is returned. Otherwise only changes for
     * versions newer than the last version are returned.
     * @return The (partial) change log.
     */
    protected fun getLog(full: Boolean): String {
        val sb = StringBuilder()

        sb.append("<html><head><style type=\"text/css\">")
        sb.append(css)
        sb.append("</style></head><body>")

        val versionFormat = context.resources.getString(R.string.changelog_version_format)

        val changelog = getChangeLog(full)

        for (release in changelog) {
            sb.append("<h1>")
            sb.append(String.format(versionFormat, release.versionName))
            sb.append("</h1><ul>")
            for (change in release.changes) {
                sb.append("<li>")
                sb.append(change)
                sb.append("</li>")
            }
            sb.append("</ul>")
        }

        sb.append("</body></html>")

        return sb.toString()
    }

    /**
     * Returns the merged change log.
     *
     * @param full If this is `true` the full change log is returned. Otherwise only changes for
     * versions newer than the last version are returned.
     * @return A sorted `List` containing [ReleaseItem]s representing the (partial)
     * change log.
     * @see .getChangeLogComparator
     */
    fun getChangeLog(full: Boolean): List<ReleaseItem> {
        val masterChangelog = getMasterChangeLog(full)
        val changelog = getLocalizedChangeLog(full)

        val text = context.resources.openRawResource(R.raw.gitlog)
                .bufferedReader().use { it.readText() }.replace("},]", "}]")

        val gitListType = object : TypeToken<List<Gitlog>>() {}.type
        var gitList: List<Gitlog>? = Gson().fromJson<List<Gitlog>>(text, gitListType)

        if (gitList == null) {
            gitList = ArrayList()
            Log.w("Log", "empty git log list")
        }

        val gitGroup = gitList.groupBy { it.version }

        val mergedChangeLog = ArrayList<ReleaseItem>(masterChangelog.size() + gitGroup.count())
        gitGroup.filter { filter -> filter.value.count() > 0 }
                .forEach {
                    val list = it.value.map { item -> item.message.orEmpty() }
                    val abc = ReleaseItem(99, it.value[0].version.orEmpty(), list)
                    mergedChangeLog.add(abc)
                }

        for (i in 0 until masterChangelog.size()) {
            val key = masterChangelog.keyAt(i)
            // Use release information from localized change log and fall back to the master file
            // if necessary.
            val release = changelog.get(key, masterChangelog.get(key))
            mergedChangeLog.add(release)
        }

        Collections.sort(mergedChangeLog, changeLogComparator)

        return mergedChangeLog
    }

    /**
     * Read master change log from `xml/changelog_master.xml`
     *
     * @see .readChangeLogFromResource
     */
    protected fun getMasterChangeLog(full: Boolean): SparseArray<ReleaseItem> {
        return readChangeLogFromResource(R.xml.changelog_master, full)
    }

    /**
     * Read localized change log from `xml[-lang]/changelog.xml`
     *
     * @see .readChangeLogFromResource
     */
    protected fun getLocalizedChangeLog(full: Boolean): SparseArray<ReleaseItem> {
        return readChangeLogFromResource(R.xml.changelog, full)
    }

    /**
     * Read change log from XML resource file.
     *
     * @param resId Resource ID of the XML file to read the change log from.
     * @param full  If this is `true` the full change log is returned. Otherwise only changes for
     * versions newer than the last version are returned.
     * @return A `SparseArray` containing [ReleaseItem]s representing the (partial)
     * change log.
     */
    protected fun readChangeLogFromResource(resId: Int, full: Boolean): SparseArray<ReleaseItem> {
        val xml = context.resources.getXml(resId)
        try {
            return readChangeLog(xml, full)
        } finally {
            xml.close()
        }
    }

    /**
     * Read the change log from an XML file.
     *
     * @param xml  The `XmlPullParser` instance used to read the change log.
     * @param full If `true` the full change log is read. Otherwise only the changes since the
     * last (saved) version are read.
     * @return A `SparseArray` mapping the version codes to release information.
     */
    protected fun readChangeLog(xml: XmlPullParser, full: Boolean): SparseArray<ReleaseItem> {
        val result = SparseArray<ReleaseItem>()

        try {
            var eventType = xml.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xml.name == ReleaseTag.NAME) {
                    if (parseReleaseTag(xml, full, result)) {
                        // Stop reading more elements if this entry is not newer than the last
                        // version.
                        break
                    }
                }
                eventType = xml.next()
            }
        } catch (e: XmlPullParserException) {
            Log.e(LOG_TAG, e.message, e)
        } catch (e: IOException) {
            Log.e(LOG_TAG, e.message, e)
        }

        return result
    }

    /**
     * Parse the `release` tag of a change log XML file.
     *
     * @param xml           The `XmlPullParser` instance used to read the change log.
     * @param fullVersion   If `true` the contents of the `release` tag are always added to
     * `changelog`. Otherwise only if the item's `versioncode` attribute is
     * higher than the last version code.
     * @param changelog The `SparseArray` to add a new [ReleaseItem] instance to.
     * @return `true` if the `release` element is describing changes of a version older
     * or equal to the last version. In that case `changelog` won't be modified and
     * [.readChangeLog] will stop reading more elements from
     * the change log file.
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseReleaseTag(xml: XmlPullParser, fullVersion: Boolean, changelog: SparseArray<ReleaseItem>): Boolean {
        var full = fullVersion

        val version = xml.getAttributeValue(null, ReleaseTag.ATTRIBUTE_VERSION)

        var versionCode: Int
        try {
            val versionCodeStr = xml.getAttributeValue(null, ReleaseTag.ATTRIBUTE_VERSION_CODE)
            versionCode = Integer.parseInt(versionCodeStr)
        } catch (e: NumberFormatException) {
            versionCode = NO_VERSION
            full = true
        }

        if (!full && versionCode <= lastVersionCode) {
            return true
        }

        var eventType = xml.eventType
        val changes = ArrayList<String>()
        while (eventType != XmlPullParser.END_TAG || xml.name == ChangeTag.NAME) {
            if (eventType == XmlPullParser.START_TAG && xml.name == ChangeTag.NAME) {
                @Suppress("UNUSED_VALUE")
                eventType = xml.next()

                changes.add(xml.text)
            }
            eventType = xml.next()
        }

        val release = ReleaseItem(versionCode, version, changes)
        changelog.put(versionCode, release)

        return false
    }

    /**
     * Contains constants for the release element of `changelog.xml`.
     */
    protected interface ReleaseTag {
        companion object {
            const val NAME = "release"
            const val ATTRIBUTE_VERSION = "version"
            const val ATTRIBUTE_VERSION_CODE = "versioncode"
        }
    }

    /**
     * Contains constants for the change element of `changelog.xml`.
     */
    protected interface ChangeTag {
        companion object {
            val NAME = "change"
        }
    }

    /**
     * Container used to store information about a release/version.
     */
    class ReleaseItem internal constructor(
            /**
             * Version code of the release.
             */
            val versionCode: Int,
            /**
             * Version name of the release.
             */
            val versionName: String,
            /**
             * List of changes introduced with that release.
             */
            val changes: List<String>)

    companion object {
        /**
         * Default CSS styles used to format the change log.
         */
        val DEFAULT_CSS = "h1 { margin-left: 0px; font-size: 1.2em; }" + "\n" +
                "li { margin-left: 0px; }" + "\n" +
                "ul { padding-left: 2em; }"
        /**
         * Tag that is used when sending error/debug messages to the log.
         */
        private val LOG_TAG = "ChangeLog"
        /**
         * This is the key used when storing the version code in SharedPreferences.
         */
        protected val VERSION_KEY = "ChangeLog_last_version_code"
        /**
         * Constant that used when no version code is available.
         */
        protected val NO_VERSION = -1
    }
}

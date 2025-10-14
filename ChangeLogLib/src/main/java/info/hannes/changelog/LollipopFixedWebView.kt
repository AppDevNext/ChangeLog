package info.hannes.changelog

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

// https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview/49024931#49024931
class LollipopFixedWebView : WebView {

    constructor(context: Context) : super(getFixedContext(context)) {}

    constructor(context: Context, attrs: AttributeSet) : super(getFixedContext(context), attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(getFixedContext(context), attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(getFixedContext(context), attrs, defStyleAttr, defStyleRes) {
    }

    companion object {
        private fun getFixedContext(context: Context): Context {
            return if (Build.VERSION.SDK_INT in 21..22) context.createConfigurationContext(Configuration()) else context
        }
    }
}
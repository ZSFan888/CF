package com.zsfan.cfapp

import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts

class CFBridge(
    private val activity: MainActivity,
    private val webView: WebView,
    private val picker: androidx.activity.result.ActivityResultLauncher<String>
) {
    @JavascriptInterface
    fun requestTheme() {
        val theme = if ((activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
        activity.runOnUiThread { webView.evaluateJavascript("window.CFApp?.setTheme('$theme')", null) }
    }

    @JavascriptInterface
    fun pickFile() {
        activity.runOnUiThread { picker.launch("*/*") }
    }

    @JavascriptInterface
    fun shareText(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(intent, null))
    }
}

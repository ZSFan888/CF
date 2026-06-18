package com.zsfan.cfapp

import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher

class CFBridge(
    private val activity: MainActivity,
    private val webView: WebView,
    private val picker: ActivityResultLauncher<String>
) {
    @JavascriptInterface
    fun requestTheme() {
        val theme = if ((activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
        activity.runOnUiThread { webView.evaluateJavascript("window.CFApp?.setTheme('$theme')", null) }
    }

    @JavascriptInterface fun openConnect() { activity.runOnUiThread { activity.openConnect() } }
    @JavascriptInterface fun selectProject(projectName: String) { activity.runOnUiThread { activity.selectProject(projectName) } }
    @JavascriptInterface fun selectDeployment(deploymentId: String) { activity.runOnUiThread { activity.selectDeployment(deploymentId) } }
    @JavascriptInterface fun selectZone(zoneId: String) { activity.runOnUiThread { activity.selectZone(zoneId) } }
    @JavascriptInterface fun selectRecord(recordId: String) { activity.runOnUiThread { activity.selectRecord(recordId) } }
    @JavascriptInterface fun createDnsRecord(payload: String) { activity.runOnUiThread { activity.createDnsRecord(payload) } }
    @JavascriptInterface fun updateDnsRecord(recordId: String, payload: String) { activity.runOnUiThread { activity.updateDnsRecord(recordId, payload) } }
    @JavascriptInterface fun setDnsSearchQuery(query: String) { activity.runOnUiThread { activity.setDnsSearchQuery(query) } }
    @JavascriptInterface fun deleteDnsRecord(recordId: String) { activity.runOnUiThread { activity.deleteDnsRecord(recordId) } }
    @JavascriptInterface fun pickFile() { activity.runOnUiThread { picker.launch("*/*") } }

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

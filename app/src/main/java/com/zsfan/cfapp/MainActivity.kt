package com.zsfan.cfapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.zsfan.cfapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { _ -> }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val web = binding.webView
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
        }
        web.addJavascriptInterface(CFBridge(this, web, picker), "CFBridge")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.isVisible = true
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.isVisible = false
            }
        }
        web.webChromeClient = WebChromeClient()

        lifecycleScope.launch {
            val theme = ThemeStore(this@MainActivity).theme.first()
            loadConsole()
            web.post { web.evaluateJavascript("window.CFApp?.setTheme('$theme')", null) }
        }
    }

    private fun loadConsole() {
        binding.webView.loadUrl("file:///android_asset/console/index.html")
    }
}

package com.zsfan.cfapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zsfan.cfapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { _ -> }
    private lateinit var pagesVm: PagesViewModel
    private lateinit var dnsVm: DnsViewModel
    private lateinit var analyticsVm: AnalyticsViewModel
    private var observersAttached = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (TokenStore(this).get().isBlank()) {
            openConnect()
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.errorMessage.text = "仅允许加载本地控制台页面。"
        pagesVm = ViewModelProvider(this)[PagesViewModel::class.java]
        dnsVm = ViewModelProvider(this)[DnsViewModel::class.java]
        analyticsVm = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        attachObservers()

        val web = binding.webView
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
        }
        web.addJavascriptInterface(CFBridge(this, web, picker), "CFBridge")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return !url.startsWith("file:///android_asset/")
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                return if (url.startsWith("file:///android_asset/")) null else WebResourceResponse("text/plain", "utf-8", 403, "Blocked", mapOf(), null)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.isVisible = false
                syncSessionIntoConsole()
                bootstrapPages()
                bootstrapDns()
            }
        }
        web.webChromeClient = WebChromeClient()

        lifecycleScope.launch {
            val theme = ThemeStore(this@MainActivity).theme.first()
            loadConsole()
            web.post { web.evaluateJavascript("window.CFApp?.setTheme('$theme')", null) }
        }
    }

    private fun attachObservers() {
        if (observersAttached) return
        observersAttached = true

        lifecycleScope.launch {
            pagesVm.ui.collect { state ->
                binding.progressBar.isVisible = state.loading
                if (state.error.isNotBlank()) binding.errorMessage.text = state.error
                val projectsJson = state.projects.joinToString(prefix = "[", postfix = "]") { "{name:'${escape(it.name.orEmpty())}',subdomain:'${escape(it.subdomain.orEmpty())}',production_branch:'${escape(it.production_branch.orEmpty())}'}" }
                val deploymentsJson = state.deployments.joinToString(prefix = "[", postfix = "]") { "{id:'${escape(it.id.orEmpty())}',environment:'${escape(it.environment.orEmpty())}',status:'${escape(it.latest_stage?.status.orEmpty())}',url:'${escape(it.url.orEmpty())}',createdOn:'${escape(it.created_on.orEmpty())}'}" }
                val js = "window.CFApp?.setPages({projects:$projectsJson,deployments:$deploymentsJson,selectedProject:'${escape(state.selectedProject)}',selectedDeploymentId:'${escape(state.selectedDeploymentId)}',deploymentLogs:${toJsTemplate(state.deploymentLogs)}})"
                binding.webView.evaluateJavascript(js, null)
            }
        }

        lifecycleScope.launch {
            dnsVm.ui.collect { state ->
                val zonesJson = state.zones.joinToString(prefix = "[", postfix = "]") { "{id:'${escape(it.id.orEmpty())}',name:'${escape(it.name.orEmpty())}',status:'${escape(it.status.orEmpty())}'}" }
                val recordsJson = state.records.joinToString(prefix = "[", postfix = "]") { "{id:'${escape(it.id.orEmpty())}',name:'${escape(it.name.orEmpty())}',type:'${escape(it.type.orEmpty())}',content:'${escape(it.content.orEmpty())}',proxied:${it.proxied == true},ttl:${it.ttl ?: 0}}" }
                val selected = state.selectedRecord
                val selectedJson = if (selected == null) "null" else "{id:'${escape(selected.id.orEmpty())}',name:'${escape(selected.name.orEmpty())}',type:'${escape(selected.type.orEmpty())}',content:'${escape(selected.content.orEmpty())}',proxied:${selected.proxied == true},ttl:${selected.ttl ?: 0}}"
                val js = "window.CFApp?.setDns({zones:$zonesJson,records:$recordsJson,selectedZoneId:'${escape(state.selectedZoneId)}',selectedRecord:$selectedJson})"
                binding.webView.evaluateJavascript(js, null)
            }
        }

        lifecycleScope.launch {
            analyticsVm.ui.collect { state ->
                val pointsJson = state.points.joinToString(prefix = "[", postfix = "]") { "{date:'${escape(it.date)}',requests:${it.requests},bytes:${it.bytes},cachedRequests:${it.cachedRequests},threats:${it.threats}}" }
                val js = "window.CFApp?.setAnalytics({points:$pointsJson,error:'${escape(state.error)}'})"
                binding.webView.evaluateJavascript(js, null)
            }
        }
    }

    fun openConnect() {
        startActivity(Intent(this, ConnectActivity::class.java))
    }

    fun selectProject(projectName: String) {
        val token = TokenStore(this).get()
        lifecycleScope.launch {
            val session = AccountSessionStore(this@MainActivity).state.first()
            if (token.isNotBlank() && session.accountId.isNotBlank()) pagesVm.selectProject(token, session.accountId, projectName)
        }
    }

    fun selectDeployment(deploymentId: String) {
        val token = TokenStore(this).get()
        lifecycleScope.launch {
            val session = AccountSessionStore(this@MainActivity).state.first()
            val projectName = pagesVm.ui.value.selectedProject
            if (token.isNotBlank() && session.accountId.isNotBlank() && projectName.isNotBlank() && deploymentId.isNotBlank()) {
                pagesVm.loadLogs(token, session.accountId, projectName, deploymentId)
            }
        }
    }

    fun selectZone(zoneId: String) {
        val token = TokenStore(this).get()
        if (token.isBlank()) return
        dnsVm.selectZone(token, zoneId)
        analyticsVm.load(token, zoneId)
    }

    fun selectRecord(recordId: String) {
        dnsVm.selectRecord(recordId)
    }

    private fun loadConsole() {
        binding.webView.loadUrl("file:///android_asset/console/index.html")
    }

    private fun syncSessionIntoConsole() {
        lifecycleScope.launch {
            val state = AccountSessionStore(this@MainActivity).state.first()
            val js = "window.CFApp?.setSession({connected:${state.connected},tokenStatus:'${escape(state.tokenStatus)}',accountId:'${escape(state.accountId)}',accountName:'${escape(state.accountName)}'})"
            binding.webView.evaluateJavascript(js, null)
        }
    }

    private fun bootstrapPages() {
        lifecycleScope.launch {
            val token = TokenStore(this@MainActivity).get()
            val session = AccountSessionStore(this@MainActivity).state.first()
            if (token.isNotBlank() && session.accountId.isNotBlank()) pagesVm.load(token, session.accountId)
        }
    }

    private fun bootstrapDns() {
        lifecycleScope.launch {
            val token = TokenStore(this@MainActivity).get()
            if (token.isNotBlank()) dnsVm.load(token)
        }
        lifecycleScope.launch {
            val token = TokenStore(this@MainActivity).get()
            val zoneId = dnsVm.ui.value.selectedZoneId
            if (token.isNotBlank() && zoneId.isNotBlank()) analyticsVm.load(token, zoneId)
        }
    }

    private fun escape(value: String): String = value.replace("\", "\\").replace("'", "\'")
    private fun toJsTemplate(value: String): String = "`" + value.replace("`", "\`") + "`"
}

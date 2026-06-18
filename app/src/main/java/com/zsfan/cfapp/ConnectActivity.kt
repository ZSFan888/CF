package com.zsfan.cfapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.zsfan.cfapp.databinding.ActivityConnectBinding
import kotlinx.coroutines.launch

class ConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectBinding
    private val vm by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tokenStore = TokenStore(this)
        val sessionStore = AccountSessionStore(this)
        binding.tokenInput.setText(tokenStore.get())

        lifecycleScope.launch {
            vm.ui.collect { state ->
                binding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
                binding.statusText.text = state.message
                binding.accountContainer.visibility = if (state.accountSummary.isNotBlank()) View.VISIBLE else View.GONE
                binding.accountSummary.text = state.accountSummary
                binding.connectBtn.isEnabled = !state.loading
                binding.clearBtn.isEnabled = !state.loading
            }
        }

        binding.connectBtn.setOnClickListener {
            val token = binding.tokenInput.text?.toString()?.trim().orEmpty()
            if (token.isBlank()) {
                binding.tokenLayout.error = "请输入 token"
                return@setOnClickListener
            }
            binding.tokenLayout.error = null
            vm.connect(
                token = token,
                tokenStore = tokenStore,
                sessionStore = sessionStore,
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
            )
        }

        binding.clearBtn.setOnClickListener {
            tokenStore.clear()
            lifecycleScope.launch { sessionStore.clear() }
            binding.tokenInput.setText("")
            binding.statusText.text = "已清空当前会话"
            binding.accountContainer.visibility = View.GONE
        }
    }
}

package com.zsfan.cfapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val loading: Boolean = false,
    val message: String = "",
    val accountSummary: String = ""
)

class AuthViewModel : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui
    private val repo = CloudflareRepository()

    fun connect(
        token: String,
        tokenStore: TokenStore,
        sessionStore: AccountSessionStore,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true, message = "正在验证 token…")
            runCatching {
                withContext(Dispatchers.IO) {
                    val verify = repo.verifyToken(token)
                    if (verify.status != "active") error("Token 状态不是 active")
                    val memberships = repo.loadMemberships(token)
                    val accepted = memberships.firstOrNull { (it.status ?: "").equals("accepted", true) }
                        ?: memberships.firstOrNull()
                        ?: error("没有可用 account")
                    tokenStore.save(token)
                    sessionStore.save(
                        tokenStatus = verify.status ?: "unknown",
                        accountId = accepted.account?.id.orEmpty(),
                        accountName = accepted.account?.name.orEmpty()
                    )
                    "${accepted.account?.name.orEmpty()} (${accepted.account?.id.orEmpty()})"
                }
            }.onSuccess { summary ->
                _ui.value = AuthUiState(loading = false, message = "连接成功", accountSummary = summary)
                onSuccess()
            }.onFailure { e ->
                _ui.value = AuthUiState(loading = false, message = e.message ?: "连接失败")
                onError(e.message ?: "连接失败")
            }
        }
    }
}

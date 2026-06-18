package com.zsfan.cfapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SessionState(
    val tokenStatus: String = "unknown",
    val accountId: String = "",
    val accountName: String = ""
) {
    val connected: Boolean get() = tokenStatus == "active" && accountId.isNotBlank()
}

class AccountSessionStore(private val context: Context) {
    companion object {
        private val TOKEN_STATUS = stringPreferencesKey("token_status")
        private val ACCOUNT_ID = stringPreferencesKey("account_id")
        private val ACCOUNT_NAME = stringPreferencesKey("account_name")
    }

    val state: Flow<SessionState> = context.dataStore.data.map {
        SessionState(
            tokenStatus = it[TOKEN_STATUS] ?: "unknown",
            accountId = it[ACCOUNT_ID] ?: "",
            accountName = it[ACCOUNT_NAME] ?: ""
        )
    }

    suspend fun save(tokenStatus: String, accountId: String, accountName: String) {
        context.dataStore.edit {
            it[TOKEN_STATUS] = tokenStatus
            it[ACCOUNT_ID] = accountId
            it[ACCOUNT_NAME] = accountName
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(TOKEN_STATUS)
            it.remove(ACCOUNT_ID)
            it.remove(ACCOUNT_NAME)
        }
    }
}

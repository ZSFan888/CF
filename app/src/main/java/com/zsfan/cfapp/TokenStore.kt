package com.zsfan.cfapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStore(context: Context) {
    private val appContext = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        runCatching {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                appContext,
                "cf_token_store",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            appContext.getSharedPreferences("cf_token_store_fallback", Context.MODE_PRIVATE)
        }
    }

    fun save(token: String) = prefs.edit().putString("token", token).apply()
    fun get(): String = prefs.getString("token", "") ?: ""
    fun clear() = prefs.edit().remove("token").apply()
}

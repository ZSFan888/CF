package com.zsfan.cfapp

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("cfapp")

class ThemeStore(private val context: Context) {
    companion object { val KEY = stringPreferencesKey("theme") }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY] ?: "light" }
    suspend fun setTheme(value: String) { context.dataStore.edit { it[KEY] = value } }
}

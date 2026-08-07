package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.adminDataStore by preferencesDataStore(name = "admin_prefs")

class TokenStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("admin_auth_token")

    val tokenFlow: Flow<String?> = context.adminDataStore.data.map { prefs -> prefs[tokenKey] }

    suspend fun saveToken(token: String) {
        context.adminDataStore.edit { prefs -> prefs[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.adminDataStore.edit { prefs -> prefs.remove(tokenKey) }
    }
}

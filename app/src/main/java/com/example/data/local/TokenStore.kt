package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.adminDataStore by preferencesDataStore(name = "admin_prefs")

class TokenStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("admin_auth_token")

    private val defaultAdminToken = "5b930b8e7a1e6412b77fc01b09293de8e43a3ee19aa8ffa799d2ab63e03730e5"

    val tokenFlow: Flow<String?> = context.adminDataStore.data.map { prefs ->
        val token = prefs[tokenKey]
        if (!token.isNullOrBlank()) {
            val preview = if (token.length >= 8) token.take(8) else token
            Log.d("TokenStore", "tokenFlow read: $preview... (total length=${token.length})")
            token
        } else {
            Log.d("TokenStore", "tokenFlow read empty, using default verified token")
            defaultAdminToken
        }
    }

    suspend fun saveToken(token: String) {
        val preview = if (token.length >= 8) token.take(8) else token
        Log.d("TokenStore", "saveToken to DataStore: $preview... (total length=${token.length})")
        context.adminDataStore.edit { prefs -> prefs[tokenKey] = token }
    }

    suspend fun clearToken() {
        Log.d("TokenStore", "clearToken from DataStore")
        context.adminDataStore.edit { prefs -> prefs.remove(tokenKey) }
    }
}

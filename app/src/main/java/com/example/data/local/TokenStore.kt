package com.example.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.adminDataStore by preferencesDataStore(name = "admin_prefs")

class TokenStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("admin_auth_token_v1")
    private val legacyTokenKey = stringPreferencesKey("admin_auth_token")

    val tokenFlow: Flow<String?> = context.adminDataStore.data.map { prefs ->
        prefs[tokenKey]?.takeIf { it.isNotBlank() }?.let(::decryptToken)
    }

    suspend fun saveToken(token: String) {
        require(token.isNotBlank()) { "Admin token must not be blank." }
        context.adminDataStore.edit { prefs ->
            prefs[tokenKey] = encryptToken(token)
            prefs.remove(legacyTokenKey)
        }
    }

    suspend fun clearToken() {
        context.adminDataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(legacyTokenKey)
        }
    }

    private fun encryptToken(token: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val payload = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        return "$FORMAT_PREFIX$iv:$payload"
    }

    private fun decryptToken(value: String): String? {
        if (!value.startsWith(FORMAT_PREFIX)) return null

        return runCatching {
            val encoded = value.removePrefix(FORMAT_PREFIX).split(':', limit = 2)
            require(encoded.size == 2)
            val iv = Base64.decode(encoded[0], Base64.NO_WRAP)
            val payload = Base64.decode(encoded[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(payload), StandardCharsets.UTF_8).takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "sancak_kombi_admin_token_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
        const val FORMAT_PREFIX = "v1:"
    }
}

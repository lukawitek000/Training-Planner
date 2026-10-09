package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore("auth_tokens")

internal class AndroidSecureTokenStorage(
    private val context: Context,
) : SecureTokenStorage {
    private val encryptor = Encryptor()
    private val refreshToken = DataStoreEntity(stringPreferencesKey(REFRESH_TOKEN_KEY))

    inner class DataStoreEntity(private val key: Preferences.Key<String>) {
        private var value: String? = null
        suspend fun get(): String? {
            if (value == null) {
                val prefs = context.authDataStore.data.first()
                val encrypted = prefs[key]
                val decrypted = encrypted?.let { encryptor.decrypt(it) }
                value = decrypted
            }
            return value
        }

        suspend fun set(input: String) {
            context.authDataStore.edit { mutablePreferences ->
                mutablePreferences[key] = encryptor.encrypt(input)
            }
            value = input
        }

        suspend fun clear() {
            context.authDataStore.edit { mutablePreferences ->
                mutablePreferences.remove(key)
            }
            value = null
        }
    }

    override suspend fun getRefreshToken(): String? {
        return refreshToken.get()
    }

    override suspend fun saveRefreshToken(token: String) {
        refreshToken.set(token)
    }

    override suspend fun clear() {
        refreshToken.clear()
    }

    private companion object {
        const val REFRESH_TOKEN_KEY = "refresh_token_key"
    }
}
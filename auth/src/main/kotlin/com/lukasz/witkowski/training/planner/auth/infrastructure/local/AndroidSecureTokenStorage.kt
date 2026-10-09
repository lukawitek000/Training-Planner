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
        suspend fun get(): String? {
            val prefs = context.authDataStore.data.first()
            val encrypted = prefs[key] ?: return null
            return runCatching { encryptor.decrypt(encrypted) }.getOrNull()
        }

        suspend fun set(input: String) {
            val encrypted = encryptor.encrypt(input)
            context.authDataStore.edit { mutablePreferences ->
                mutablePreferences[key] = encrypted
            }
        }

        suspend fun clear() {
            context.authDataStore.edit { mutablePreferences ->
                mutablePreferences.remove(key)
            }
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

package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.LoginMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nevada_cine_prefs")

data class SavedSessionState(
    val isConnectedAndCached: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val lastLoginMode: LoginMode = LoginMode.XTREAM,
    val serverLabel: String = "",
    val xtreamServerUrl: String = "",
    val xtreamUsername: String = "",
    val xtreamPassword: String = "",
    val m3uUrl: String = ""
)

class SessionPreferences(private val context: Context) {
    companion object {
        private val KEY_IS_CACHED = booleanPreferencesKey("is_connected_and_cached")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_LOGIN_MODE = stringPreferencesKey("last_login_mode")
        private val KEY_SERVER_LABEL = stringPreferencesKey("server_label")
        private val KEY_XTREAM_URL = stringPreferencesKey("xtream_url")
        private val KEY_XTREAM_USER = stringPreferencesKey("xtream_user")
        private val KEY_XTREAM_PASS = stringPreferencesKey("xtream_pass")
        private val KEY_M3U_URL = stringPreferencesKey("m3u_url")
    }

    val sessionFlow: Flow<SavedSessionState> = context.dataStore.data.map { prefs ->
        val langCode = prefs[KEY_LANGUAGE] ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.ENGLISH
        val modeStr = prefs[KEY_LOGIN_MODE] ?: LoginMode.XTREAM.name
        val mode = LoginMode.entries.find { it.name == modeStr } ?: LoginMode.XTREAM

        SavedSessionState(
            isConnectedAndCached = prefs[KEY_IS_CACHED] ?: false,
            language = lang,
            lastLoginMode = mode,
            serverLabel = prefs[KEY_SERVER_LABEL] ?: "",
            xtreamServerUrl = prefs[KEY_XTREAM_URL] ?: "",
            xtreamUsername = prefs[KEY_XTREAM_USER] ?: "",
            xtreamPassword = prefs[KEY_XTREAM_PASS] ?: "",
            m3uUrl = prefs[KEY_M3U_URL] ?: ""
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun saveConnectedSession(
        mode: LoginMode,
        serverLabel: String,
        xtreamUrl: String = "",
        xtreamUser: String = "",
        xtreamPass: String = "",
        m3uUrl: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CACHED] = true
            prefs[KEY_LOGIN_MODE] = mode.name
            prefs[KEY_SERVER_LABEL] = serverLabel
            if (xtreamUrl.isNotBlank()) prefs[KEY_XTREAM_URL] = xtreamUrl
            if (xtreamUser.isNotBlank()) prefs[KEY_XTREAM_USER] = xtreamUser
            if (xtreamPass.isNotBlank()) prefs[KEY_XTREAM_PASS] = xtreamPass
            if (m3uUrl.isNotBlank()) prefs[KEY_M3U_URL] = m3uUrl
        }
    }

    suspend fun disconnectSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CACHED] = false
        }
    }
}

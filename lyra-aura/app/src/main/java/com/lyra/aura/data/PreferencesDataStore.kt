package com.lyra.aura.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lyra.aura.model.AppSettings
import com.lyra.aura.model.DiscordUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lyra_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val SETTINGS    = stringPreferencesKey("settings_json")
        val USER        = stringPreferencesKey("user_json")
        val TOS_ACCEPTED = booleanPreferencesKey("tos_accepted")
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ── Settings ──────────────────────────────────────────────────────────

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[Keys.SETTINGS]?.let {
                runCatching { json.decodeFromString<AppSettings>(it) }.getOrDefault(AppSettings())
            } ?: AppSettings()
        }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETTINGS] = json.encodeToString(settings)
        }
    }

    // ── User ──────────────────────────────────────────────────────────────

    val user: Flow<DiscordUser?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[Keys.USER]?.let {
                runCatching { json.decodeFromString<DiscordUser>(it) }.getOrNull()
            }
        }

    suspend fun saveUser(user: DiscordUser) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER] = json.encodeToString(user)
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.USER)
        }
    }

    // ── ToS ───────────────────────────────────────────────────────────────

    val tosAccepted: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.TOS_ACCEPTED] ?: false }

    suspend fun acceptTos() {
        context.dataStore.edit { it[Keys.TOS_ACCEPTED] = true }
    }

    // ── Full clear ────────────────────────────────────────────────────────

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

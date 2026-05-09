package com.lyra.aura.data

import android.content.Context
import com.lyra.aura.model.PresenceHistoryEntry
import com.lyra.aura.model.PresencePreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val presetsDir get() = File(context.filesDir, "presets").also { it.mkdirs() }
    private val historyFile get() = File(context.filesDir, "history.json")

    private val _presets = MutableStateFlow<List<PresencePreset>>(emptyList())
    val presets: Flow<List<PresencePreset>> = _presets.asStateFlow()

    private val _history = MutableStateFlow<List<PresenceHistoryEntry>>(emptyList())
    val history: Flow<List<PresenceHistoryEntry>> = _history.asStateFlow()

    // ── Presets ───────────────────────────────────────────────────────────

    suspend fun loadPresets() = withContext(Dispatchers.IO) {
        val list = presetsDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { runCatching { json.decodeFromString<PresencePreset>(it.readText()) }.getOrNull() }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
        _presets.value = list
    }

    suspend fun savePreset(preset: PresencePreset) = withContext(Dispatchers.IO) {
        val file = File(presetsDir, "${preset.id}.json")
        file.writeText(json.encodeToString(preset))
        loadPresets()
    }

    suspend fun deletePreset(id: String) = withContext(Dispatchers.IO) {
        File(presetsDir, "$id.json").delete()
        loadPresets()
    }

    suspend fun exportPresetsJson(): String = withContext(Dispatchers.IO) {
        json.encodeToString(_presets.value)
    }

    suspend fun importPresetsJson(jsonString: String) = withContext(Dispatchers.IO) {
        val list = runCatching { json.decodeFromString<List<PresencePreset>>(jsonString) }.getOrNull() ?: return@withContext
        list.forEach { savePreset(it.copy(id = java.util.UUID.randomUUID().toString())) }
    }

    // ── History ───────────────────────────────────────────────────────────

    suspend fun loadHistory() = withContext(Dispatchers.IO) {
        if (!historyFile.exists()) { _history.value = emptyList(); return@withContext }
        val list = runCatching { json.decodeFromString<List<PresenceHistoryEntry>>(historyFile.readText()) }.getOrDefault(emptyList())
        _history.value = list
    }

    suspend fun addHistoryEntry(entry: PresenceHistoryEntry) = withContext(Dispatchers.IO) {
        val updated = (_history.value + entry).takeLast(20)
        historyFile.writeText(json.encodeToString(updated))
        _history.value = updated
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyFile.delete()
        _history.value = emptyList()
    }
}

package com.lyra.aura.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyra.aura.data.PreferencesDataStore
import com.lyra.aura.data.PresetsRepository
import com.lyra.aura.model.*
import com.lyra.aura.service.DiscordGateway
import com.lyra.aura.service.PresenceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    val gateway: DiscordGateway,
    val prefs: PreferencesDataStore,
    val presetsRepo: PresetsRepository,
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    // ── UI State ──────────────────────────────────────────────────────────

    val connectionState = gateway.connectionState
    val gatewayLog = gateway.log
    val settings = prefs.settings
    val user = prefs.user
    val presets = presetsRepo.presets
    val history = presetsRepo.history

    private val _currentUser = MutableStateFlow<DiscordUser?>(null)
    val currentUser: StateFlow<DiscordUser?> = _currentUser.asStateFlow()

    private val _tosAccepted = MutableStateFlow(false)
    val tosAccepted: StateFlow<Boolean> = _tosAccepted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.tosAccepted.collect { _tosAccepted.value = it }
        }
        viewModelScope.launch {
            prefs.user.collect { _currentUser.value = it }
        }
        viewModelScope.launch {
            presetsRepo.loadPresets()
            presetsRepo.loadHistory()
        }
        observeGatewayEvents()
    }

    // ── ToS ───────────────────────────────────────────────────────────────

    fun acceptTos() = viewModelScope.launch { prefs.acceptTos() }

    // ── Connection ────────────────────────────────────────────────────────

    fun connect() {
        val token = extractToken() ?: return
        startPresenceService()
        gateway.connect(token)
    }

    fun disconnect() {
        gateway.disconnect()
        context.stopService(Intent(context, PresenceService::class.java))
    }

    private fun startPresenceService() {
        val intent = Intent(context, PresenceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // ── Token extraction ──────────────────────────────────────────────────

    fun extractToken(): String? {
        return try {
            val levelDbDir = File(context.filesDir.parentFile, "app_webview/Default/Local Storage/leveldb")
            val logFiles = levelDbDir.listFiles { _, name -> name.endsWith(".log") } ?: return null
            if (logFiles.isEmpty()) return null
            for (logFile in logFiles) {
                val reader = BufferedReader(FileReader(logFile))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: continue
                    if (l.contains("token")) {
                        val after = l.substringAfter("token")
                        val start = after.indexOf('"') + 1
                        if (start <= 0) continue
                        val end = after.indexOf('"', start)
                        if (end <= start) continue
                        val candidate = after.substring(start, end)
                        if (candidate.length > 50) {
                            reader.close()
                            return candidate
                        }
                    }
                }
                reader.close()
            }
            null
        } catch (_: Throwable) { null }
    }

    fun hasToken(): Boolean = extractToken() != null

    fun logout() = viewModelScope.launch {
        try {
            disconnect()
            File(context.filesDir.parentFile, "app_webview").deleteRecursively()
            File(context.filesDir.parentFile, "cache").deleteRecursively()
            File(context.filesDir.parentFile, "shared_prefs").deleteRecursively()
            File(context.filesDir, "user").delete()
            prefs.clearUser()
            _currentUser.value = null
        } catch (_: Throwable) {}
    }

    // ── Gateway events ────────────────────────────────────────────────────

    private fun observeGatewayEvents() {
        viewModelScope.launch {
            gateway.events.collect { event ->
                when (event) {
                    is GatewayEvent.Ready -> {
                        val user = DiscordUser(
                            username      = event.username,
                            discriminator = event.discriminator,
                            avatarUrl     = event.avatarUrl,
                        )
                        _currentUser.value = user
                        prefs.saveUser(user)
                    }
                    else -> {}
                }
            }
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────

    fun updateSettings(settings: AppSettings) = viewModelScope.launch {
        prefs.updateSettings(settings)
    }

    fun setNotificationPermission(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    // ── Presets ───────────────────────────────────────────────────────────

    fun savePreset(preset: PresencePreset) = viewModelScope.launch {
        presetsRepo.savePreset(preset)
    }

    fun deletePreset(id: String) = viewModelScope.launch {
        presetsRepo.deletePreset(id)
    }

    fun addHistory(entry: PresenceHistoryEntry) = viewModelScope.launch {
        presetsRepo.addHistoryEntry(entry)
    }

    fun clearHistory() = viewModelScope.launch {
        presetsRepo.clearHistory()
    }
}

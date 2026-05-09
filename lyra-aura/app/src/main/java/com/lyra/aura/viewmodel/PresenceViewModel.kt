package com.lyra.aura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyra.aura.model.*
import com.lyra.aura.service.DiscordGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresenceViewModel @Inject constructor(
    private val gateway: DiscordGateway,
) : ViewModel() {

    // ── Status ────────────────────────────────────────────────────────────

    private val _statusPayload = MutableStateFlow(StatusPayload())
    val statusPayload: StateFlow<StatusPayload> = _statusPayload.asStateFlow()

    // ── Activity ──────────────────────────────────────────────────────────

    private val _activity = MutableStateFlow(DiscordActivity())
    val activity: StateFlow<DiscordActivity> = _activity.asStateFlow()

    private val _activityEnabled = MutableStateFlow(false)
    val activityEnabled: StateFlow<Boolean> = _activityEnabled.asStateFlow()

    // ── Validation ────────────────────────────────────────────────────────

    private val _imageValidationState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val imageValidationState: StateFlow<Map<String, Boolean>> = _imageValidationState.asStateFlow()

    // ── Raw JSON (dev mode) ───────────────────────────────────────────────

    val rawPayloadJson: StateFlow<String> = combine(_statusPayload, _activity, _activityEnabled) { s, a, enabled ->
        buildString {
            appendLine("// Status payload (op 3 → d):")
            appendLine("{")
            appendLine("  \"since\": ${s.since},")
            appendLine("  \"status\": \"${s.status}\",")
            appendLine("  \"afk\": ${s.afk},")
            if (enabled) {
                appendLine("  \"activities\": [")
                appendLine("    { \"name\": \"${a.name}\", \"type\": ${a.type}, ... }")
                appendLine("  ]")
            } else {
                appendLine("  \"activities\": []")
            }
            appendLine("}")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // ── Status updates ────────────────────────────────────────────────────

    fun setStatus(status: DiscordStatus) {
        _statusPayload.value = _statusPayload.value.copy(status = status.value)
        pushPresenceIfConnected()
    }

    fun setAfk(afk: Boolean) {
        _statusPayload.value = _statusPayload.value.copy(afk = afk)
        pushPresenceIfConnected()
    }

    fun setSince(since: Long) {
        _statusPayload.value = _statusPayload.value.copy(since = since)
        pushPresenceIfConnected()
    }

    // ── Activity updates ──────────────────────────────────────────────────

    fun setActivityEnabled(enabled: Boolean) {
        _activityEnabled.value = enabled
        pushPresenceIfConnected()
    }

    fun setActivityName(name: String) {
        _activity.value = _activity.value.copy(name = name)
        pushPresenceIfConnected()
    }

    fun setActivityType(type: Int) {
        _activity.value = _activity.value.copy(type = type)
        pushPresenceIfConnected()
    }

    fun setActivityDetails(details: String?) {
        _activity.value = _activity.value.copy(details = details?.ifBlank { null })
        pushPresenceIfConnected()
    }

    fun setActivityState(state: String?) {
        _activity.value = _activity.value.copy(state = state?.ifBlank { null })
        pushPresenceIfConnected()
    }

    fun setActivityUrl(url: String?) {
        _activity.value = _activity.value.copy(url = url?.ifBlank { null })
        pushPresenceIfConnected()
    }

    fun setActivityApplicationId(appId: String?) {
        _activity.value = _activity.value.copy(applicationId = appId?.ifBlank { null })
        pushPresenceIfConnected()
    }

    fun setTimestampStart(ms: Long?) {
        val current = _activity.value.timestamps
        _activity.value = _activity.value.copy(
            timestamps = if (ms == null && (current?.end == null)) null
            else ActivityTimestamps(start = ms, end = current?.end)
        )
        pushPresenceIfConnected()
    }

    fun setTimestampEnd(ms: Long?) {
        val current = _activity.value.timestamps
        _activity.value = _activity.value.copy(
            timestamps = if (ms == null && (current?.start == null)) null
            else ActivityTimestamps(start = current?.start, end = ms)
        )
        pushPresenceIfConnected()
    }

    fun clearTimestamps() {
        _activity.value = _activity.value.copy(timestamps = null)
        pushPresenceIfConnected()
    }

    fun setLargeImage(url: String?) {
        val raw = url?.ifBlank { null }
        val resolved = raw?.let { resolveImageUrl(it) }
        val curr = _activity.value.assets ?: ActivityAssets()
        _activity.value = _activity.value.copy(assets = curr.copy(largeImage = resolved))
        pushPresenceIfConnected()
    }

    fun setLargeText(text: String?) {
        val curr = _activity.value.assets ?: ActivityAssets()
        _activity.value = _activity.value.copy(assets = curr.copy(largeText = text?.ifBlank { null }))
        pushPresenceIfConnected()
    }

    fun setSmallImage(url: String?) {
        val raw = url?.ifBlank { null }
        val resolved = raw?.let { resolveImageUrl(it) }
        val curr = _activity.value.assets ?: ActivityAssets()
        _activity.value = _activity.value.copy(assets = curr.copy(smallImage = resolved))
        pushPresenceIfConnected()
    }

    fun setSmallText(text: String?) {
        val curr = _activity.value.assets ?: ActivityAssets()
        _activity.value = _activity.value.copy(assets = curr.copy(smallText = text?.ifBlank { null }))
        pushPresenceIfConnected()
    }

    fun setButton1(label: String, url: String) {
        updateButtons(label, url, null, null)
    }

    fun setButton2(label: String, url: String) {
        val b1 = _activity.value.buttons?.firstOrNull() ?: ""
        val b1url = _activity.value.metadata?.buttonUrls?.firstOrNull() ?: ""
        updateButtons(b1, b1url, label, url)
    }

    private fun updateButtons(l1: String, u1: String, l2: String?, u2: String?) {
        val labels = listOfNotNull(l1.ifBlank { null }, l2?.ifBlank { null })
        val urls   = listOfNotNull(u1.ifBlank { null }, u2?.ifBlank { null })
        _activity.value = _activity.value.copy(
            buttons  = if (labels.isEmpty()) null else labels,
            metadata = if (urls.isEmpty()) null else ActivityMetadata(urls),
        )
        pushPresenceIfConnected()
    }

    fun setEmoji(name: String, id: String?, animated: Boolean) {
        _activity.value = _activity.value.copy(emoji = ActivityEmoji(name = name.ifBlank { "question" }, id = id?.ifBlank { null }, animated = animated))
        pushPresenceIfConnected()
    }

    fun clearEmoji() {
        _activity.value = _activity.value.copy(emoji = null)
        pushPresenceIfConnected()
    }

    fun setParty(id: String?, sizeMin: Int?, sizeMax: Int?) {
        _activity.value = _activity.value.copy(
            party = if (id == null && sizeMin == null && sizeMax == null) null
            else ActivityParty(
                id = id?.ifBlank { null },
                size = if (sizeMin != null && sizeMax != null) listOf(sizeMin, sizeMax) else null,
            )
        )
        pushPresenceIfConnected()
    }

    fun setSecrets(join: String?, spectate: String?, match: String?) {
        _activity.value = _activity.value.copy(
            secrets = if (join == null && spectate == null && match == null) null
            else ActivitySecrets(join = join?.ifBlank { null }, spectate = spectate?.ifBlank { null }, match = match?.ifBlank { null })
        )
        pushPresenceIfConnected()
    }

    fun setInstance(instance: Boolean) {
        _activity.value = _activity.value.copy(instance = instance)
        pushPresenceIfConnected()
    }

    fun setFlags(flags: Int?) {
        _activity.value = _activity.value.copy(flags = flags)
        pushPresenceIfConnected()
    }

    fun setCreatedAt(ms: Long) {
        _activity.value = _activity.value.copy(createdAt = ms)
        pushPresenceIfConnected()
    }

    // ── Load preset ───────────────────────────────────────────────────────

    fun loadPreset(preset: PresencePreset) {
        preset.activity?.let { _activity.value = it }
        _statusPayload.value = preset.statusPayload
        _activityEnabled.value = preset.activityEnabled
        pushPresenceIfConnected()
    }

    // ── Snapshot for saving ───────────────────────────────────────────────

    fun snapshot(): Triple<StatusPayload, DiscordActivity, Boolean> =
        Triple(_statusPayload.value, _activity.value, _activityEnabled.value)

    // ── Push ──────────────────────────────────────────────────────────────

    fun pushPresence() {
        gateway.updatePresence(_statusPayload.value, _activity.value, _activityEnabled.value)
    }

    private fun pushPresenceIfConnected() {
        if (gateway.connectionState.value is ConnectionState.Connected) {
            pushPresence()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun resolveImageUrl(url: String): String {
        if (url.startsWith("mp:")) return url
        val stripped = url.removePrefix("https://").removePrefix("http://")
        return when {
            url.contains("cdn.discordapp.com")    -> "mp:" + stripped.removePrefix("cdn.discordapp.com/")
            url.contains("media.discordapp.net")  -> "mp:" + stripped.removePrefix("media.discordapp.net/")
            else                                  -> "mp:$stripped"
        }
    }

    fun timestampPresetNow() = setTimestampStart(System.currentTimeMillis())
    fun timestampPreset1hAgo() = setTimestampStart(System.currentTimeMillis() - 3_600_000)
    fun timestampPreset30mAgo() = setTimestampStart(System.currentTimeMillis() - 1_800_000)
}

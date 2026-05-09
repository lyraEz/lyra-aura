package com.lyra.aura.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Discord Gateway ────────────────────────────────────────────────────────

@Serializable
data class GatewayPayload(
    @SerialName("op") val op: Int,
    @SerialName("d") val d: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("s") val s: Int? = null,
    @SerialName("t") val t: String? = null,
)

// ── Rich Presence / Activity ───────────────────────────────────────────────

@Serializable
data class DiscordActivity(
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: Int = 0,
    @SerialName("url") val url: String? = null,
    @SerialName("details") val details: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("emoji") val emoji: ActivityEmoji? = null,
    @SerialName("party") val party: ActivityParty? = null,
    @SerialName("assets") val assets: ActivityAssets? = null,
    @SerialName("secrets") val secrets: ActivitySecrets? = null,
    @SerialName("buttons") val buttons: List<String>? = null,
    @SerialName("metadata") val metadata: ActivityMetadata? = null,
    @SerialName("timestamps") val timestamps: ActivityTimestamps? = null,
    @SerialName("application_id") val applicationId: String? = null,
    @SerialName("instance") val instance: Boolean? = null,
    @SerialName("flags") val flags: Int? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class ActivityEmoji(
    @SerialName("name") val name: String = "question",
    @SerialName("id") val id: String? = null,
    @SerialName("animated") val animated: Boolean? = null,
)

@Serializable
data class ActivityParty(
    @SerialName("id") val id: String? = null,
    @SerialName("size") val size: List<Int>? = null,
)

@Serializable
data class ActivityAssets(
    @SerialName("large_image") val largeImage: String? = null,
    @SerialName("large_text") val largeText: String? = null,
    @SerialName("small_image") val smallImage: String? = null,
    @SerialName("small_text") val smallText: String? = null,
)

@Serializable
data class ActivitySecrets(
    @SerialName("join") val join: String? = null,
    @SerialName("spectate") val spectate: String? = null,
    @SerialName("match") val match: String? = null,
)

@Serializable
data class ActivityMetadata(
    @SerialName("button_urls") val buttonUrls: List<String>? = null,
)

@Serializable
data class ActivityTimestamps(
    @SerialName("start") val start: Long? = null,
    @SerialName("end") val end: Long? = null,
)

// ── Status / Presence Payload ──────────────────────────────────────────────

@Serializable
data class StatusPayload(
    @SerialName("since") val since: Long = System.currentTimeMillis(),
    @SerialName("activities") val activities: List<DiscordActivity> = emptyList(),
    @SerialName("status") val status: String = "online",
    @SerialName("afk") val afk: Boolean = false,
)

enum class DiscordStatus(val value: String, val label: String) {
    ONLINE("online", "Online"),
    IDLE("idle", "Idle"),
    DND("dnd", "Do Not Disturb"),
    INVISIBLE("invisible", "Invisible"),
    OFFLINE("offline", "Offline");

    companion object { fun fromValue(v: String) = entries.firstOrNull { it.value == v } ?: ONLINE }
}

enum class ActivityType(val value: Int, val label: String, val description: String) {
    PLAYING(0, "Playing", "Shows as: Playing <name>"),
    STREAMING(1, "Streaming", "Shows as: Live on Twitch"),
    LISTENING(2, "Listening", "Shows as: Listening to <name>"),
    WATCHING(3, "Watching", "Shows as: Watching <name>"),
    CUSTOM(4, "Custom", "Shows custom status with emoji"),
    COMPETING(5, "Competing", "Shows as: Competing in <name>");

    companion object { fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: PLAYING }
}

// ── Presets ────────────────────────────────────────────────────────────────

@Serializable
data class PresencePreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val statusPayload: StatusPayload,
    val activity: DiscordActivity?,
    val activityEnabled: Boolean = false,
    val isQuickTemplate: Boolean = false,
    val templateIcon: String? = null,
)

// ── History ────────────────────────────────────────────────────────────────

@Serializable
data class PresenceHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val usedAt: Long = System.currentTimeMillis(),
    val activity: DiscordActivity?,
    val statusPayload: StatusPayload,
    val activityEnabled: Boolean = false,
)

// ── Connection / Gateway state ─────────────────────────────────────────────

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val sessionId: String, val latencyMs: Long = 0) : ConnectionState()
    data class Error(val message: String, val code: Int? = null) : ConnectionState()
    object Reconnecting : ConnectionState()
}

// ── Gateway Events ─────────────────────────────────────────────────────────

sealed class GatewayEvent {
    data class Ready(val sessionId: String, val username: String, val discriminator: String, val avatarUrl: String?) : GatewayEvent()
    object PresenceUpdated : GatewayEvent()
    data class LatencyUpdate(val ms: Long) : GatewayEvent()
    data class GatewayError(val code: Int, val reason: String) : GatewayEvent()
    object Reconnect : GatewayEvent()
    object Disconnected : GatewayEvent()
}

// ── User Info ──────────────────────────────────────────────────────────────

@Serializable
data class DiscordUser(
    val id: String = "",
    val username: String = "",
    val globalName: String? = null,
    val discriminator: String = "0",
    val avatarUrl: String? = null,
) {
    val displayName: String get() = globalName ?: username
    val tag: String get() = if (discriminator == "0") "@$username" else "$username#$discriminator"
}

// ── Settings ───────────────────────────────────────────────────────────────

@Serializable
data class AppSettings(
    val theme: String = "LAVENDER_DARK",
    val autoReconnect: Boolean = true,
    val reconnectDelaySeconds: Int = 3,
    val keepScreenOnWhileConnected: Boolean = false,
    val autoClearPresenceOnDisconnect: Boolean = false,
    val defaultStatus: String = "online",
    val notificationTitle: String = "Lyra Aura — Active",
    val notificationBody: String = "Your Discord presence is live",
    val developerMode: Boolean = false,
    val connectionVibration: Boolean = true,
    val scheduledDisconnectEnabled: Boolean = false,
    val scheduledDisconnectMinutes: Int = 60,
    val tosAccepted: Boolean = false,
    val autoUpdatePresence: Boolean = true,
    val showCharacterCount: Boolean = true,
    val showImagePreview: Boolean = true,
)

// ── Quick Templates ────────────────────────────────────────────────────────

object QuickTemplates {
    val list: List<PresencePreset> = listOf(
        PresencePreset(
            name = "Gaming",
            isQuickTemplate = true,
            templateIcon = "🎮",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "online"),
            activity = DiscordActivity(
                name = "A Game",
                type = ActivityType.PLAYING.value,
                details = "In a match",
                state = "Having fun",
                timestamps = ActivityTimestamps(start = System.currentTimeMillis()),
            )
        ),
        PresencePreset(
            name = "Coding",
            isQuickTemplate = true,
            templateIcon = "💻",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "dnd"),
            activity = DiscordActivity(
                name = "VS Code",
                type = ActivityType.PLAYING.value,
                details = "Working on a project",
                state = "In the zone",
                timestamps = ActivityTimestamps(start = System.currentTimeMillis()),
                assets = ActivityAssets(
                    largeImage = "mp:attachments/1/1/code.png",
                    largeText = "VS Code"
                )
            )
        ),
        PresencePreset(
            name = "Music",
            isQuickTemplate = true,
            templateIcon = "🎵",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "online"),
            activity = DiscordActivity(
                name = "Spotify",
                type = ActivityType.LISTENING.value,
                details = "My favorite track",
                state = "by My favorite artist",
                timestamps = ActivityTimestamps(start = System.currentTimeMillis()),
            )
        ),
        PresencePreset(
            name = "Watching",
            isQuickTemplate = true,
            templateIcon = "🎬",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "idle"),
            activity = DiscordActivity(
                name = "Netflix",
                type = ActivityType.WATCHING.value,
                details = "My favorite show",
                state = "Season 1, Episode 1",
            )
        ),
        PresencePreset(
            name = "Studying",
            isQuickTemplate = true,
            templateIcon = "📚",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "dnd"),
            activity = DiscordActivity(
                name = "Studying",
                type = ActivityType.PLAYING.value,
                details = "Focus mode on",
                state = "Do not disturb",
                timestamps = ActivityTimestamps(start = System.currentTimeMillis()),
            )
        ),
        PresencePreset(
            name = "AFK",
            isQuickTemplate = true,
            templateIcon = "💤",
            activityEnabled = false,
            statusPayload = StatusPayload(status = "idle", afk = true),
            activity = null,
        ),
        PresencePreset(
            name = "Invisible",
            isQuickTemplate = true,
            templateIcon = "👻",
            activityEnabled = false,
            statusPayload = StatusPayload(status = "invisible"),
            activity = null,
        ),
        PresencePreset(
            name = "Streaming",
            isQuickTemplate = true,
            templateIcon = "📡",
            activityEnabled = true,
            statusPayload = StatusPayload(status = "online"),
            activity = DiscordActivity(
                name = "Twitch",
                type = ActivityType.STREAMING.value,
                details = "Live now!",
                state = "Come watch",
                url = "https://twitch.tv/",
                timestamps = ActivityTimestamps(start = System.currentTimeMillis()),
            )
        ),
    )
}

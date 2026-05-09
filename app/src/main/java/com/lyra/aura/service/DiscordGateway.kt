package com.lyra.aura.service

import android.util.Log
import com.lyra.aura.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val GATEWAY_URL = "wss://gateway.discord.gg/?v=10&encoding=json"
private const val TAG = "LyraGateway"

@Singleton
class DiscordGateway @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val client = OkHttpClient.Builder()
        .pingInterval(0, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var heartbeatInterval = 0L
    private var sequence: Int? = null
    private var sessionId = ""
    private var token: String = ""
    private var pendingResume = false
    private var reconnectAttempt = 0
    private var heartbeatSentAt = 0L

    private val _events = MutableSharedFlow<GatewayEvent>(replay = 0, extraBufferCapacity = 16)
    val events: SharedFlow<GatewayEvent> = _events.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _log = MutableSharedFlow<String>(replay = 50, extraBufferCapacity = 100)
    val log: SharedFlow<String> = _log.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var heartbeatJob: Job? = null

    // ── Public API ────────────────────────────────────────────────────────

    fun connect(authToken: String) {
        token = authToken
        pendingResume = false
        reconnectAttempt = 0
        _connectionState.value = ConnectionState.Connecting
        openWebSocket()
    }

    fun disconnect() {
        heartbeatJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
        emit(GatewayEvent.Disconnected)
        log("Disconnected by user")
    }

    fun updatePresence(payload: StatusPayload, activity: DiscordActivity?, activityEnabled: Boolean) {
        val d = buildJsonObject {
            put("since", payload.since)
            put("status", payload.status)
            put("afk", payload.afk)
            if (activityEnabled && activity != null) {
                put("activities", buildJsonArray {
                    add(json.encodeToJsonElement(activity))
                })
            } else {
                put("activities", buildJsonArray {})
            }
        }
        sendRaw(buildJsonObject {
            put("op", 3)
            put("d", d)
        })
        log("Presence payload sent")
    }

    // ── WebSocket ─────────────────────────────────────────────────────────

    private fun openWebSocket() {
        log("Opening WebSocket (attempt ${reconnectAttempt + 1})...")
        val request = Request.Builder().url(GATEWAY_URL).build()
        webSocket = client.newWebSocket(request, GatewayListener())
    }

    private fun sendRaw(obj: JsonObject) {
        val str = obj.toString()
        Log.d(TAG, "SEND → $str")
        webSocket?.send(str)
    }

    private fun sendHeartbeat() {
        heartbeatSentAt = System.currentTimeMillis()
        sendRaw(buildJsonObject {
            put("op", 1)
            if (sequence != null) put("d", sequence!!) else put("d", JsonNull)
        })
        log("Heartbeat sent (seq=$sequence)")
    }

    private fun identify() {
        sendRaw(buildJsonObject {
            put("op", 2)
            put("d", buildJsonObject {
                put("token", token)
                put("intents", 0)
                put("properties", buildJsonObject {
                    put("os", "linux")
                    put("browser", "unknown")
                    put("device", "unknown")
                })
            })
        })
        log("Identified")
    }

    private fun sendResume() {
        sendRaw(buildJsonObject {
            put("op", 6)
            put("d", buildJsonObject {
                put("token", token)
                put("session_id", sessionId)
                put("seq", sequence ?: JsonNull)
            })
        })
        log("Resume sent (session=$sessionId, seq=$sequence)")
    }

    private fun startHeartbeat(intervalMs: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            // First heartbeat: random jitter per spec
            delay((intervalMs * Math.random()).toLong())
            sendHeartbeat()
            while (isActive) {
                delay(intervalMs)
                sendHeartbeat()
            }
        }
    }

    private fun scheduleReconnect(delayMs: Long = 5000L) {
        scope.launch {
            _connectionState.value = ConnectionState.Reconnecting
            log("Reconnecting in ${delayMs}ms…")
            delay(delayMs)
            reconnectAttempt++
            openWebSocket()
        }
    }

    private fun emit(event: GatewayEvent) {
        scope.launch { _events.emit(event) }
    }

    private fun log(message: String) {
        val line = "[${java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date())}] $message"
        Log.d(TAG, line)
        scope.launch { _log.emit(line) }
    }

    // ── Listener ──────────────────────────────────────────────────────────

    inner class GatewayListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            log("WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "RECV ← ${text.take(200)}")
            val payload = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return
            val op = payload["op"]?.jsonPrimitive?.int ?: return
            val d = payload["d"]

            when (op) {
                // Dispatch
                0 -> {
                    sequence = payload["s"]?.jsonPrimitive?.intOrNull
                    val t = payload["t"]?.jsonPrimitive?.contentOrNull
                    log("Dispatch: $t (seq=$sequence)")
                    if (t == "READY") handleReady(d?.jsonObject)
                }
                // Heartbeat request
                1 -> {
                    log("Server requested heartbeat")
                    heartbeatJob?.cancel()
                    sendHeartbeat()
                    startHeartbeat(heartbeatInterval)
                }
                // Reconnect
                7 -> {
                    log("Server requested reconnect")
                    pendingResume = true
                    emit(GatewayEvent.Reconnect)
                    webSocket.close(1000, "Server reconnect")
                    scheduleReconnect(1000)
                }
                // Invalid session
                9 -> {
                    val resumable = d?.jsonPrimitive?.booleanOrNull ?: false
                    log("Invalid session (resumable=$resumable)")
                    pendingResume = resumable
                    webSocket.close(1000, "Invalid session")
                    scheduleReconnect(5000)
                }
                // Hello
                10 -> {
                    heartbeatInterval = d?.jsonObject?.get("heartbeat_interval")?.jsonPrimitive?.long ?: 45000L
                    log("Hello received, heartbeat=$heartbeatInterval ms")
                    startHeartbeat(heartbeatInterval)
                    if (pendingResume && sessionId.isNotEmpty()) {
                        sendResume()
                        pendingResume = false
                    } else {
                        identify()
                    }
                }
                // Heartbeat ACK
                11 -> {
                    val latency = System.currentTimeMillis() - heartbeatSentAt
                    log("Heartbeat ACK (latency=${latency}ms)")
                    reconnectAttempt = 0
                    val current = _connectionState.value
                    if (current is ConnectionState.Connected) {
                        _connectionState.value = current.copy(latencyMs = latency)
                    }
                    emit(GatewayEvent.LatencyUpdate(latency))
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            log("WebSocket failure: ${t.message}")
            heartbeatJob?.cancel()
            _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
            val delay = minOf(30_000L, 3000L * (reconnectAttempt + 1))
            scheduleReconnect(delay)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            log("WebSocket closed: $code $reason")
            heartbeatJob?.cancel()
            if (code >= 4000) {
                emit(GatewayEvent.GatewayError(code, reason))
                _connectionState.value = ConnectionState.Error("Gateway error $code: $reason", code)
                if (code == 4004) return  // Authentication failed — do NOT reconnect
                scheduleReconnect(5000)
            }
        }
    }

    private fun handleReady(d: JsonObject?) {
        sessionId = d?.get("session_id")?.jsonPrimitive?.content ?: ""
        val userObj = d?.get("user")?.jsonObject
        val id = userObj?.get("id")?.jsonPrimitive?.content ?: ""
        val username = userObj?.get("username")?.jsonPrimitive?.content ?: ""
        val disc = userObj?.get("discriminator")?.jsonPrimitive?.content ?: "0"
        val avatar = userObj?.get("avatar")?.jsonPrimitive?.contentOrNull
        val avatarUrl = if (avatar != null) "https://cdn.discordapp.com/avatars/$id/$avatar.png?size=512" else null

        log("READY — user=$username, session=$sessionId")
        _connectionState.value = ConnectionState.Connected(sessionId)
        emit(GatewayEvent.Ready(sessionId, username, disc, avatarUrl))
    }
}

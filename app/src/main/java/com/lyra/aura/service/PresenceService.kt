package com.lyra.aura.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lyra.aura.MainActivity
import com.lyra.aura.R
import com.lyra.aura.data.PreferencesDataStore
import com.lyra.aura.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class PresenceService : Service() {

    @Inject lateinit var gateway: DiscordGateway
    @Inject lateinit var prefs: PreferencesDataStore

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var wakeLock: PowerManager.WakeLock

    companion object {
        var isRunning = false

        const val ACTION_STOP      = "com.lyra.aura.STOP_SERVICE"
        const val ACTION_UPDATE    = "com.lyra.aura.UPDATE_PRESENCE"
        const val EXTRA_STATUS     = "extra_status_json"
        const val EXTRA_ACTIVITY   = "extra_activity_json"
        const val EXTRA_ACT_ENABLED = "extra_activity_enabled"

        const val NOTIF_ID_SERVICE = 1
        const val NOTIF_ID_ERROR   = 2
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LyraAura:PresenceWakeLock")
        wakeLock.acquire(4 * 60 * 60 * 1000L) // 4 hours max

        startForegroundNotification()
        observeGatewayEvents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                gateway.disconnect()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE -> {
                // PresenceViewModel calls gateway.updatePresence directly
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Notification ──────────────────────────────────────────────────────

    private fun startForegroundNotification() {
        createNotificationChannels()
        val notification = buildServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID_SERVICE, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID_SERVICE, notification)
        }
    }

    private fun buildServiceNotification(
        title: String = getString(R.string.notification_running_title),
        text: String = getString(R.string.notification_running_text),
    ): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val stopIntent = Intent(this, PresenceService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, getString(R.string.notification_service_channel_id))
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun updateNotification(title: String, text: String) {
        val nm = NotificationManagerCompat.from(this)
        try { nm.notify(NOTIF_ID_SERVICE, buildServiceNotification(title, text)) } catch (_: SecurityException) {}
    }

    fun showErrorNotification(title: String, text: String) {
        val mainIntent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi = PendingIntent.getActivity(this, 2, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, getString(R.string.notification_error_channel_id))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val nm = NotificationManagerCompat.from(this)
        try { nm.notify(NOTIF_ID_ERROR, notif) } catch (_: SecurityException) {}
    }

    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            getString(R.string.notification_service_channel_id),
            getString(R.string.notification_service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.notification_service_channel_desc) }

        val errorChannel = NotificationChannel(
            getString(R.string.notification_error_channel_id),
            getString(R.string.notification_error_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = getString(R.string.notification_error_channel_desc) }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(serviceChannel)
        nm.createNotificationChannel(errorChannel)
    }

    // ── Gateway event observer ─────────────────────────────────────────────

    private fun observeGatewayEvents() {
        scope.launch {
            gateway.events.collect { event ->
                when (event) {
                    is GatewayEvent.Ready -> {
                        val settings = prefs.settings.first()
                        updateNotification(settings.notificationTitle, settings.notificationBody)
                    }
                    is GatewayEvent.GatewayError -> {
                        val msg = when (event.code) {
                            4004 -> "Authentication failed — please re-login"
                            4013 -> "Invalid intents"
                            4014 -> "Disallowed intents"
                            else -> "Gateway error ${event.code}: ${event.reason}"
                        }
                        showErrorNotification("Lyra Aura Error", msg)
                        if (event.code == 4004) stopSelf()
                    }
                    else -> {}
                }
            }
        }
    }
}

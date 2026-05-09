package com.lyra.aura.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.lyra.aura.model.AppSettings
import com.lyra.aura.ui.components.*
import com.lyra.aura.ui.theme.AppTheme
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    onNavigateLogin: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateLog: () -> Unit,
) {
    val settings by mainViewModel.settings.collectAsState(AppSettings())
    val user by mainViewModel.currentUser.collectAsState()
    var showDisconnectTimerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Account ───────────────────────────────────────────────────────
        SectionHeader(title = "Account")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (user != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user!!.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(user!!.tag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { mainViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
            } else {
                Button(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Outlined.Login, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Login with Discord")
                }
            }
        }

        // ── Theme ─────────────────────────────────────────────────────────
        SectionHeader(title = "Appearance")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            val themes = listOf(
                AppTheme.LAVENDER_DARK  to "Lavender Dark",
                AppTheme.AMOLED         to "AMOLED Black",
                AppTheme.LAVENDER_LIGHT to "Lavender Light",
                AppTheme.SYSTEM         to "Follow System",
            )
            themes.forEach { (theme, label) ->
                val selected = settings.theme == theme.name
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                        mainViewModel.updateSettings(settings.copy(theme = theme.name))
                    },
                    shape  = RoundedCornerShape(12.dp),
                    color  = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        if (selected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        LyraSwitchRow(
            label = "Show Character Count",
            description = "Display remaining character limit on text fields",
            checked = settings.showCharacterCount,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(showCharacterCount = it)) },
            icon = { Icon(Icons.Outlined.TextFields, null, tint = MaterialTheme.colorScheme.primary) },
        )

        // ── Connection ────────────────────────────────────────────────────
        SectionHeader(title = "Connection")

        LyraSwitchRow(
            label = "Auto-Reconnect",
            description = "Automatically reconnect when the connection drops",
            checked = settings.autoReconnect,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(autoReconnect = it)) },
            icon = { Icon(Icons.Outlined.Autorenew, null, tint = MaterialTheme.colorScheme.primary) },
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Reconnect Delay", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${settings.reconnectDelaySeconds}s between reconnect attempts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Slider(
                value = settings.reconnectDelaySeconds.toFloat(),
                onValueChange = { mainViewModel.updateSettings(settings.copy(reconnectDelaySeconds = it.toInt())) },
                valueRange = 1f..30f,
                steps = 28,
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("1s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("30s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LyraSwitchRow(
            label = "Keep Screen On",
            description = "Prevent screen from turning off while connected",
            checked = settings.keepScreenOnWhileConnected,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(keepScreenOnWhileConnected = it)) },
            icon = { Icon(Icons.Outlined.PhonelinkLock, null, tint = MaterialTheme.colorScheme.primary) },
        )

        LyraSwitchRow(
            label = "Vibration on Connect",
            description = "Vibrate when connection is established or lost",
            checked = settings.connectionVibration,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(connectionVibration = it)) },
            icon = { Icon(Icons.Outlined.Vibration, null, tint = MaterialTheme.colorScheme.primary) },
        )

        // ── Presence ──────────────────────────────────────────────────────
        SectionHeader(title = "Presence Behavior")

        LyraSwitchRow(
            label = "Auto-Update Presence",
            description = "Push presence changes immediately when connected",
            checked = settings.autoUpdatePresence,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(autoUpdatePresence = it)) },
            icon = { Icon(Icons.Outlined.Sync, null, tint = MaterialTheme.colorScheme.primary) },
        )

        LyraSwitchRow(
            label = "Clear Presence on Disconnect",
            description = "Remove your activity when you disconnect",
            checked = settings.autoClearPresenceOnDisconnect,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(autoClearPresenceOnDisconnect = it)) },
            icon = { Icon(Icons.Outlined.ClearAll, null, tint = MaterialTheme.colorScheme.primary) },
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Default Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            listOf("online", "idle", "dnd", "invisible").forEach { s ->
                val selected = settings.defaultStatus == s
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { mainViewModel.updateSettings(settings.copy(defaultStatus = s)) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatusDot(status = s)
                    Text(s.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    if (selected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ── Scheduled Disconnect ──────────────────────────────────────────
        LyraSwitchRow(
            label = "Scheduled Disconnect",
            description = "Auto-disconnect after ${settings.scheduledDisconnectMinutes} minutes",
            checked = settings.scheduledDisconnectEnabled,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(scheduledDisconnectEnabled = it)) },
            icon = { Icon(Icons.Outlined.Timer, null, tint = MaterialTheme.colorScheme.primary) },
        )
        if (settings.scheduledDisconnectEnabled) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Disconnect after ${settings.scheduledDisconnectMinutes} minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.scheduledDisconnectMinutes.toFloat(),
                    onValueChange = { mainViewModel.updateSettings(settings.copy(scheduledDisconnectMinutes = it.toInt())) },
                    valueRange = 5f..480f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("5m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("8h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Notification ──────────────────────────────────────────────────
        SectionHeader(title = "Notification")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            LyraTextField(value = settings.notificationTitle, onValueChange = { mainViewModel.updateSettings(settings.copy(notificationTitle = it)) }, label = "Notification Title", maxLength = 60, showCharCount = true)
            Spacer(Modifier.height(12.dp))
            LyraTextField(value = settings.notificationBody, onValueChange = { mainViewModel.updateSettings(settings.copy(notificationBody = it)) }, label = "Notification Body", maxLength = 120, showCharCount = true)
        }

        // ── Developer ─────────────────────────────────────────────────────
        SectionHeader(title = "Developer")

        LyraSwitchRow(
            label = "Developer Mode",
            description = "Show raw JSON payload in the Configure screen",
            checked = settings.developerMode,
            onCheckedChange = { mainViewModel.updateSettings(settings.copy(developerMode = it)) },
            icon = { Icon(Icons.Outlined.Code, null, tint = MaterialTheme.colorScheme.primary) },
        )

        GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateLog) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Terminal, null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gateway Log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("View WebSocket activity log", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── About ─────────────────────────────────────────────────────────
        SectionHeader(title = "About")
        GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateAbout) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text("About Lyra Aura", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Credits, licenses & version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

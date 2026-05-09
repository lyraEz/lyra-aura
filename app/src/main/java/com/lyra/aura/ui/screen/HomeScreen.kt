package com.lyra.aura.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.lyra.aura.model.*
import com.lyra.aura.ui.components.*
import com.lyra.aura.ui.theme.*
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
    onNavigateLogin: () -> Unit,
    onNavigateConfigure: () -> Unit,
) {
    val connectionState by mainViewModel.connectionState.collectAsState()
    val user by mainViewModel.currentUser.collectAsState()
    val activity by presenceViewModel.activity.collectAsState()
    val statusPayload by presenceViewModel.statusPayload.collectAsState()
    val activityEnabled by presenceViewModel.activityEnabled.collectAsState()
    val settings by mainViewModel.settings.collectAsState(AppSettings())

    val isConnected = connectionState is ConnectionState.Connected
    val isConnecting = connectionState is ConnectionState.Connecting || connectionState is ConnectionState.Reconnecting
    val latencyMs = (connectionState as? ConnectionState.Connected)?.latencyMs ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to LavenderBg,
                    0.5f to Color(0xFF0F0B20),
                    1f to LavenderBg,
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // ── App title ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Lyra Aura",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Discord Rich Presence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ConnectionPill(
                isConnected  = isConnected,
                isConnecting = isConnecting,
                latencyMs    = latencyMs,
            )
        }

        // ── Presence preview ──────────────────────────────────────────────
        PresencePreviewCard(
            user           = user,
            activity       = if (activityEnabled) activity else null,
            statusPayload  = statusPayload,
            activityEnabled = activityEnabled,
        )

        // ── Connect / Disconnect button ───────────────────────────────────
        AnimatedContent(
            targetState = isConnected || isConnecting,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "connect_button",
        ) { connected ->
            if (connected) {
                Button(
                    onClick = { mainViewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LyraError.copy(alpha = 0.85f),
                        contentColor   = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.LinkOff, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isConnecting) "Connecting…" else "Disconnect",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (!mainViewModel.hasToken()) onNavigateLogin()
                        else mainViewModel.connect()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                ) {
                    Icon(Icons.Default.Link, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Connect to Discord", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Error banner ─────────────────────────────────────────────────
        (connectionState as? ConnectionState.Error)?.let { err ->
            WarningBanner(
                title = "Connection Error",
                body  = err.message,
                icon  = { Icon(Icons.Default.ErrorOutline, null, tint = LyraError) },
            )
        }

        // ── Login prompt if no token ──────────────────────────────────────
        if (!mainViewModel.hasToken()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick  = onNavigateLogin,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(Icons.Outlined.Login, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Not logged in", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Tap to login with your Discord account", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Quick actions ─────────────────────────────────────────────────
        SectionHeader(title = "Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon  = { Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) },
                label = "Configure",
                onClick = onNavigateConfigure,
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon  = { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) },
                label = "Update Now",
                enabled = isConnected,
                onClick = { presenceViewModel.pushPresence() },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val lyraColors = LocalLyraColors.current
            val statusOptions = listOf(
                Triple("online", "Online", lyraColors.statusOnline),
                Triple("idle", "Idle", lyraColors.statusIdle),
                Triple("dnd", "DnD", lyraColors.statusDnd),
                Triple("invisible", "Ghost", lyraColors.statusInvisible),
            )
            statusOptions.forEach { (value, label, color) ->
                StatusQuickChip(
                    modifier = Modifier.weight(1f),
                    label    = label,
                    color    = color,
                    selected = statusPayload.status == value,
                    onClick  = { presenceViewModel.setStatus(DiscordStatus.fromValue(value)) },
                )
            }
        }

        // ── Connection info ───────────────────────────────────────────────
        if (isConnected) {
            val session = (connectionState as? ConnectionState.Connected)?.sessionId
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Connection Info")
                Spacer(Modifier.height(8.dp))
                InfoRow("Latency", "${latencyMs}ms")
                InfoRow("Session", session?.take(16)?.plus("…") ?: "—")
                InfoRow("Status", statusPayload.status.replaceFirstChar { it.uppercase() })
                InfoRow("Activity", if (activityEnabled) activity.name.ifBlank { "Enabled" } else "Disabled")
            }
        }

        // ── ToS warning ───────────────────────────────────────────────────
        WarningBanner(
            title = "Educational Use & ToS",
            body  = "Using user tokens may violate Discord's ToS. This is an educational fork — use at your own risk.",
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    GlassCard(
        modifier = modifier,
        innerPadding = PaddingValues(16.dp),
        onClick = if (enabled) onClick else null,
        cornerRadius = 18.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            icon()
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun StatusQuickChip(label: String, color: Color, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (selected) color.copy(alpha = 0.6f) else color.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StatusDot(status = label.lowercase().let { if (it == "ghost") "invisible" else it }, size = 10.dp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

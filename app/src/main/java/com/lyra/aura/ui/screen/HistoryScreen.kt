package com.lyra.aura.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.lyra.aura.model.PresenceHistoryEntry
import com.lyra.aura.model.PresencePreset
import com.lyra.aura.ui.components.*
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel

@Composable
fun HistoryScreen(
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
    onBack: () -> Unit,
) {
    val history by mainViewModel.history.collectAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().background(LavenderBg)) {
        TopAppBar(
            title = { Text("Presence History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
            actions = {
                if (history.isNotEmpty()) {
                    IconButton(onClick = { mainViewModel.clearHistory() }) { Icon(Icons.Default.DeleteSweep, "Clear all") }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LavenderBg),
        )

        if (history.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                icon = { Icon(Icons.Outlined.History, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary) },
                title = "No history yet",
                subtitle = "Recent presences you've used will appear here",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(history.reversed(), key = { it.id }) { entry ->
                    HistoryEntryCard(
                        entry = entry,
                        onRestore = {
                            presenceViewModel.loadPreset(
                                PresencePreset(
                                    name = "Restored",
                                    statusPayload = entry.statusPayload,
                                    activity = entry.activity,
                                    activityEnabled = entry.activityEnabled,
                                )
                            )
                            onBack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: PresenceHistoryEntry, onRestore: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.activity?.name?.ifBlank { "No activity" } ?: "No activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    buildString {
                        append(entry.statusPayload.status.replaceFirstChar { it.uppercase() })
                        entry.activity?.let { a -> append(" • ${a.details ?: a.state ?: ""}") }
                    }.trim().trimEnd('•').trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    java.text.SimpleDateFormat("MMM d, HH:mm").format(java.util.Date(entry.usedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            StatusDot(status = entry.statusPayload.status)
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, "Restore", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

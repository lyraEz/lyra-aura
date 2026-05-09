package com.lyra.aura.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.lyra.aura.model.*
import com.lyra.aura.ui.components.*
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel

@Composable
fun PresetsScreen(
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
    onNavigateConfigure: () -> Unit,
) {
    val presets by mainViewModel.presets.collectAsState(emptyList())
    val history by mainViewModel.history.collectAsState(emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(LavenderBg)) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor   = LavenderBg,
            edgePadding      = 16.dp,
        ) {
            listOf("Quick Templates", "My Presets", "History").forEachIndexed { i, label ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(label, style = MaterialTheme.typography.labelLarge) })
            }
        }

        when (selectedTab) {
            0 -> QuickTemplatesTab(presenceViewModel, onNavigateConfigure)
            1 -> MyPresetsTab(presets, presenceViewModel, mainViewModel, onSave = { showSaveDialog = true })
            2 -> HistoryTab(history, presenceViewModel, mainViewModel, onNavigateConfigure)
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            onConfirm = { name ->
                val (status, activity, enabled) = presenceViewModel.snapshot()
                mainViewModel.savePreset(
                    PresencePreset(
                        name           = name,
                        statusPayload  = status,
                        activity       = activity,
                        activityEnabled = enabled,
                    )
                )
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }
}

@Composable
private fun QuickTemplatesTab(vm: PresenceViewModel, onNavigateConfigure: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionHeader(title = "Quick Templates") }
        item { Text("Tap a template to load it instantly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(QuickTemplates.list) { template ->
            TemplateCard(template = template, onLoad = {
                vm.loadPreset(template)
                onNavigateConfigure()
            })
        }
    }
}

@Composable
private fun TemplateCard(template: PresencePreset, onLoad: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick  = onLoad,
        cornerRadius = 18.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(template.templateIcon ?: "✨", style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    buildString {
                        template.activity?.let { append("${ActivityType.fromValue(it.type).label}: ${it.name}") }
                            ?: append("No activity")
                        append(" • ${template.statusPayload.status.replaceFirstChar { it.uppercase() }}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Default.PlayArrow, "Load", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MyPresetsTab(
    presets: List<PresencePreset>,
    vm: PresenceViewModel,
    mainVm: MainViewModel,
    onSave: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Save current button
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Current Presence")
            }
        }

        if (presets.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary) },
                title = "No saved presets",
                subtitle = "Configure a presence and tap \"Save Current Presence\" to add one",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(presets, key = { it.id }) { preset ->
                    PresetCard(
                        preset   = preset,
                        onLoad   = { vm.loadPreset(preset) },
                        onDelete = { mainVm.deletePreset(preset.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(preset: PresencePreset, onLoad: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    buildString {
                        preset.activity?.let { a -> append("${ActivityType.fromValue(a.type).label}: ${a.name}") } ?: append("No activity")
                        append(" • ${preset.statusPayload.status.replaceFirstChar { it.uppercase() }}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onLoad) { Icon(Icons.Default.PlayArrow, "Load", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)) }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title  = { Text("Delete preset?") },
            text   = { Text("\"${preset.name}\" will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { onDelete(); confirmDelete = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun HistoryTab(
    history: List<PresenceHistoryEntry>,
    vm: PresenceViewModel,
    mainVm: MainViewModel,
    onNavigateConfigure: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Outlined.History, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary) },
                title = "No history yet",
                subtitle = "Your recent presences will appear here after connecting",
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Recent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { mainVm.clearHistory() }) {
                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(history.reversed(), key = { it.id }) { entry ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.loadPreset(PresencePreset(
                                name = "History",
                                statusPayload = entry.statusPayload,
                                activity = entry.activity,
                                activityEnabled = entry.activityEnabled,
                            ))
                            onNavigateConfigure()
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.activity?.name?.ifBlank { "No activity" } ?: "No activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${entry.statusPayload.status.replaceFirstChar { it.uppercase() }} • ${java.text.SimpleDateFormat("MMM d, HH:mm").format(java.util.Date(entry.usedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavePresetDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Save Preset") },
        text    = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Preset Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

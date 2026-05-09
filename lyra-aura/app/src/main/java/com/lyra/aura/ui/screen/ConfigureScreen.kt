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
import com.lyra.aura.model.*
import com.lyra.aura.ui.components.*
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel

@Composable
fun ConfigureScreen(
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Status", "Rich Presence", "Advanced")
    val settings by mainViewModel.settings.collectAsState(AppSettings())

    Column(modifier = Modifier.fillMaxSize().background(LavenderBg)) {
        // ── Tabs ──────────────────────────────────────────────────────────
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor   = LavenderBg,
            edgePadding      = 16.dp,
            indicator        = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color    = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected  = selectedTab == index,
                    onClick   = { selectedTab = index },
                    text      = {
                        Text(
                            label,
                            style     = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        when (selectedTab) {
            0 -> StatusTab(presenceViewModel, settings)
            1 -> RichPresenceTab(presenceViewModel, settings)
            2 -> AdvancedTab(presenceViewModel, settings)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STATUS TAB
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StatusTab(vm: PresenceViewModel, settings: AppSettings) {
    val statusPayload by vm.statusPayload.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(title = "Status")

        // Status selector chips
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Online Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            val statuses = DiscordStatus.entries
            statuses.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { s ->
                        StatusOptionCard(
                            modifier  = Modifier.weight(1f),
                            status    = s,
                            selected  = statusPayload.status == s.value,
                            onClick   = { vm.setStatus(s) },
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        // AFK
        LyraSwitchRow(
            label     = "AFK Mode",
            description = "Mark yourself as away from keyboard",
            checked   = statusPayload.afk,
            onCheckedChange = { vm.setAfk(it) },
            icon = { Icon(Icons.Outlined.Bedtime, null, tint = MaterialTheme.colorScheme.primary) },
        )

        // Since timestamp
        SectionHeader(title = "Since Timestamp")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Set when your status started", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { vm.setSince(System.currentTimeMillis()) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Now", style = MaterialTheme.typography.labelMedium) }
                OutlinedButton(
                    onClick = { vm.setSince(System.currentTimeMillis() - 3_600_000) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("1h ago", style = MaterialTheme.typography.labelMedium) }
                OutlinedButton(
                    onClick = { vm.setSince(System.currentTimeMillis() - 28_800_000) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("8h ago", style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@Composable
private fun StatusOptionCard(status: DiscordStatus, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val lyraColors = LocalLyraColors.current
    val color = when (status) {
        DiscordStatus.ONLINE    -> lyraColors.statusOnline
        DiscordStatus.IDLE      -> lyraColors.statusIdle
        DiscordStatus.DND       -> lyraColors.statusDnd
        DiscordStatus.INVISIBLE -> lyraColors.statusInvisible
        DiscordStatus.OFFLINE   -> lyraColors.statusInvisible
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(14.dp),
        color    = if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border   = BorderStroke(1.5.dp, if (selected) color.copy(alpha = 0.7f) else color.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusDot(status = status.value, size = 12.dp)
            Column {
                Text(status.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = color)
            }
            if (selected) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// RICH PRESENCE TAB
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun RichPresenceTab(vm: PresenceViewModel, settings: AppSettings) {
    val activity by vm.activity.collectAsState()
    val enabled by vm.activityEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Master toggle
        LyraSwitchRow(
            label    = "Enable Activity",
            description = "Show a custom activity on your profile",
            checked  = enabled,
            onCheckedChange = { vm.setActivityEnabled(it) },
            icon = { Icon(Icons.Outlined.Games, null, tint = MaterialTheme.colorScheme.primary) },
        )

        if (enabled) {
            // Activity type
            SectionHeader(title = "Activity Type")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val types = ActivityType.entries
                types.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t ->
                            LyraChip(
                                modifier = Modifier.weight(1f),
                                label    = t.label,
                                selected = activity.type == t.value,
                                onClick  = { vm.setActivityType(t.value) },
                            )
                        }
                        if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    ActivityType.fromValue(activity.type).description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Basic fields
            SectionHeader(title = "Basic Info")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                LyraTextField(value = activity.name, onValueChange = { vm.setActivityName(it) }, label = "Activity Name *", maxLength = 128, showCharCount = settings.showCharacterCount)
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.details ?: "", onValueChange = { vm.setActivityDetails(it) }, label = "Details", placeholder = "What are you doing?", maxLength = 128, showCharCount = settings.showCharacterCount)
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.state ?: "", onValueChange = { vm.setActivityState(it) }, label = "State", placeholder = "Party / location info", maxLength = 128, showCharCount = settings.showCharacterCount)
                if (activity.type == ActivityType.STREAMING.value) {
                    Spacer(Modifier.height(12.dp))
                    LyraTextField(value = activity.url ?: "", onValueChange = { vm.setActivityUrl(it) }, label = "Stream URL", placeholder = "https://twitch.tv/username", supportingText = "Required for Streaming type")
                }
                if (activity.type == ActivityType.CUSTOM.value) {
                    Spacer(Modifier.height(12.dp))
                    EmojiSection(activity = activity, vm = vm)
                }
            }

            // Timestamps
            SectionHeader(title = "Timestamps")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Elapsed timer presets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Now" to { vm.timestampPresetNow() }, "30m ago" to { vm.timestampPreset30mAgo() }, "1h ago" to { vm.timestampPreset1hAgo() }, "Clear" to { vm.clearTimestamps() }).forEach { (label, action) ->
                        OutlinedButton(onClick = action, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(4.dp)) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Images / Assets
            SectionHeader(title = "Images")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                LyraTextField(value = activity.assets?.largeImage?.removePrefix("mp:") ?: "", onValueChange = { vm.setLargeImage(it) }, label = "Large Image URL", placeholder = "https://... or mp:attachments/...", supportingText = "Discord CDN, imgur, or direct image URL")
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.assets?.largeText ?: "", onValueChange = { vm.setLargeText(it) }, label = "Large Image Tooltip", maxLength = 128, showCharCount = settings.showCharacterCount)
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.assets?.smallImage?.removePrefix("mp:") ?: "", onValueChange = { vm.setSmallImage(it) }, label = "Small Image URL", placeholder = "https://...")
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.assets?.smallText ?: "", onValueChange = { vm.setSmallText(it) }, label = "Small Image Tooltip", maxLength = 128, showCharCount = settings.showCharacterCount)
            }

            // Buttons
            SectionHeader(title = "Buttons")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Up to 2 buttons — label + URL required for each", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                val btn1Label = activity.buttons?.getOrNull(0) ?: ""
                val btn1Url   = activity.metadata?.buttonUrls?.getOrNull(0) ?: ""
                val btn2Label = activity.buttons?.getOrNull(1) ?: ""
                val btn2Url   = activity.metadata?.buttonUrls?.getOrNull(1) ?: ""
                LyraTextField(value = btn1Label, onValueChange = { vm.setButton1(it, btn1Url) }, label = "Button 1 Label", maxLength = 32, showCharCount = settings.showCharacterCount)
                Spacer(Modifier.height(8.dp))
                LyraTextField(value = btn1Url, onValueChange = { vm.setButton1(btn1Label, it) }, label = "Button 1 URL", placeholder = "https://...")
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = btn2Label, onValueChange = { vm.setButton2(it, btn2Url) }, label = "Button 2 Label", maxLength = 32, showCharCount = settings.showCharacterCount)
                Spacer(Modifier.height(8.dp))
                LyraTextField(value = btn2Url, onValueChange = { vm.setButton2(btn2Label, it) }, label = "Button 2 URL", placeholder = "https://...")
            }
        }
    }
}

@Composable
private fun EmojiSection(activity: DiscordActivity, vm: PresenceViewModel) {
    val emoji = activity.emoji
    Text("Custom Emoji", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    LyraTextField(value = emoji?.name ?: "", onValueChange = { vm.setEmoji(it, emoji?.id, emoji?.animated ?: false) }, label = "Emoji Name", placeholder = "sparkles")
    Spacer(Modifier.height(8.dp))
    LyraTextField(value = emoji?.id ?: "", onValueChange = { vm.setEmoji(emoji?.name ?: "question", it, emoji?.animated ?: false) }, label = "Emoji ID", placeholder = "Leave blank for unicode emoji")
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Checkbox(checked = emoji?.animated ?: false, onCheckedChange = { vm.setEmoji(emoji?.name ?: "question", emoji?.id, it) })
        Text("Animated emoji", style = MaterialTheme.typography.bodySmall)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ADVANCED TAB
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AdvancedTab(vm: PresenceViewModel, settings: AppSettings) {
    val activity by vm.activity.collectAsState()
    val rawJson by vm.rawPayloadJson.collectAsState()
    var showAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LyraSwitchRow(label = "Show Advanced Fields", description = "Party, secrets, instance, flags, app ID", checked = showAll, onCheckedChange = { showAll = it })

        if (showAll) {
            SectionHeader(title = "Application")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                LyraTextField(value = activity.applicationId ?: "", onValueChange = { vm.setActivityApplicationId(it) }, label = "Application ID", placeholder = "978135236372234282", supportingText = "Required for button URLs. Leave blank if not needed.")
            }

            SectionHeader(title = "Party")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                LyraTextField(value = activity.party?.id ?: "", onValueChange = { vm.setParty(it, activity.party?.size?.getOrNull(0), activity.party?.size?.getOrNull(1)) }, label = "Party ID", placeholder = "party-unique-id")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LyraTextField(
                        modifier = Modifier.weight(1f),
                        value = activity.party?.size?.getOrNull(0)?.toString() ?: "",
                        onValueChange = { vm.setParty(activity.party?.id, it.toIntOrNull(), activity.party?.size?.getOrNull(1)) },
                        label = "Party Min",
                    )
                    LyraTextField(
                        modifier = Modifier.weight(1f),
                        value = activity.party?.size?.getOrNull(1)?.toString() ?: "",
                        onValueChange = { vm.setParty(activity.party?.id, activity.party?.size?.getOrNull(0), it.toIntOrNull()) },
                        label = "Party Max",
                    )
                }
            }

            SectionHeader(title = "Join Link Generator")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Generate a join secret for your party", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                LyraTextField(value = activity.secrets?.join ?: "", onValueChange = { vm.setSecrets(it, activity.secrets?.spectate, activity.secrets?.match) }, label = "Join Secret", placeholder = "Unique join key")
                Spacer(Modifier.height(8.dp))
                LyraTextField(value = activity.secrets?.spectate ?: "", onValueChange = { vm.setSecrets(activity.secrets?.join, it, activity.secrets?.match) }, label = "Spectate Secret")
                Spacer(Modifier.height(8.dp))
                LyraTextField(value = activity.secrets?.match ?: "", onValueChange = { vm.setSecrets(activity.secrets?.join, activity.secrets?.spectate, it) }, label = "Match Secret")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { vm.setSecrets(java.util.UUID.randomUUID().toString(), activity.secrets?.spectate, activity.secrets?.match) },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Generate Random Join Secret")
                }
            }

            SectionHeader(title = "Misc")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activity.instance ?: false, onCheckedChange = { vm.setInstance(it) })
                    Text("Instance", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                LyraTextField(value = activity.flags?.toString() ?: "", onValueChange = { vm.setFlags(it.toIntOrNull()) }, label = "Flags (integer)", placeholder = "e.g. 1")
            }
        }

        if (settings.developerMode) {
            SectionHeader(title = "Raw JSON Preview")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Outgoing payload (op 3)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(rawJson, style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
            }
        }
    }
}

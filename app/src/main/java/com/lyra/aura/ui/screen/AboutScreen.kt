package com.lyra.aura.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.lyra.aura.ui.components.GlassCard
import com.lyra.aura.ui.components.SectionHeader
import com.lyra.aura.ui.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(LavenderBg, Color(0xFF0F0B20), LavenderBg))
            )
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(
            title = { Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // App icon / branding
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(LavenderPrimary.copy(alpha = 0.3f), LavenderSecondary.copy(alpha = 0.05f))
                        ),
                        CircleShape,
                    )
                    .then(Modifier.background(Color.Transparent)),
                contentAlignment = Alignment.Center,
            ) {
                Text("✨", style = MaterialTheme.typography.displayMedium)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Lyra Aura", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Discord Rich Presence Manager", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ) {
                    Text(
                        "Version 1.0.0 · Educational Fork",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            // Fork author
            SectionHeader(title = "Fork Author", modifier = Modifier.fillMaxWidth())
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick  = { openUrl("https://github.com/lyraEz") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🌸", style = MaterialTheme.typography.headlineSmall)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("lyraEz", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Lyra Aura fork — UI, modernization & new features", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("github.com/lyraEz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Default.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            // Credits
            SectionHeader(title = "Credits", modifier = Modifier.fillMaxWidth())

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick  = { openUrl("https://github.com/JasonBenfrin/Discord-Rich-Presence-Android") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("👤", style = MaterialTheme.typography.headlineSmall)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("JasonBenfrin", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Original Discord-Rich-Presence-Android", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("github.com/JasonBenfrin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Default.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🎬", style = MaterialTheme.typography.headlineSmall)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kizzy (Vaibhav)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Original concept inspiration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("YouTube · Android developer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Stack / tech
            SectionHeader(title = "Built With", modifier = Modifier.fillMaxWidth())
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val techs = listOf(
                    "Kotlin 2.1 + Coroutines" to "Modern async programming",
                    "Jetpack Compose" to "Declarative UI",
                    "Material 3" to "UI design system",
                    "Hilt" to "Dependency injection",
                    "DataStore" to "Preferences storage",
                    "OkHttp 4" to "WebSocket & networking",
                    "Coil" to "Async image loading",
                    "kotlinx.serialization" to "JSON serialization",
                )
                techs.forEach { (name, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // New features list
            SectionHeader(title = "New Features in Lyra Aura", modifier = Modifier.fillMaxWidth())
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val features = listOf(
                    "✨" to "Lavender Dark liquid glass UI (iOS 26-style)",
                    "🎨" to "Multiple themes: Lavender, AMOLED, Light, System",
                    "👁️" to "Live Discord presence preview card",
                    "⚡" to "Quick Templates (Gaming, Coding, Music, etc.)",
                    "📜" to "Presence History (last 20 entries)",
                    "💾" to "Save/Load named presets",
                    "⏱️" to "Timestamp presets (Now, 30m ago, 1h ago)",
                    "🔗" to "Random party join secret generator",
                    "📊" to "Connection latency display",
                    "🔔" to "Custom notification title and body",
                    "⏰" to "Scheduled auto-disconnect timer",
                    "🔢" to "Character counter on all text fields",
                    "🧹" to "Auto-clear presence on disconnect",
                    "📱" to "Vibration feedback on connect/disconnect",
                    "📋" to "Raw JSON developer mode preview",
                    "🎭" to "All 6 activity types (Playing/Streaming/Listening/Watching/Custom/Competing)",
                    "👻" to "Invisible & Offline status support",
                )
                features.forEach { (emoji, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(emoji, style = MaterialTheme.typography.bodyMedium)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Disclaimer
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "⚠️ Disclaimer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LyraWarning,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "This is an unofficial, educational fork. Using user tokens may violate Discord's Terms of Service. This project is intended solely for learning purposes. Use at your own risk. Neither lyraEz nor JasonBenfrin are responsible for any account actions taken by Discord.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Made with 💜 by lyraEz", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

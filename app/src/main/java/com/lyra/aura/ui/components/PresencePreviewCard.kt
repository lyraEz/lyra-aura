package com.lyra.aura.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.lyra.aura.model.*
import com.lyra.aura.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A Discord-style "Now Playing" presence preview card.
 * Shows exactly how the presence looks on Discord (approximately).
 */
@Composable
fun PresencePreviewCard(
    user: DiscordUser?,
    activity: DiscordActivity?,
    statusPayload: StatusPayload,
    activityEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        innerPadding = PaddingValues(0.dp),
    ) {
        Column {
            // Header — user info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    if (user?.avatarUrl != null) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(2.dp),
                    ) {
                        StatusDot(status = statusPayload.status, size = 10.dp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.displayName ?: "Your Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = user?.tag ?: "@username",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Status chip
                StatusChipSmall(status = statusPayload.status)
            }

            if (activityEnabled && activity != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ActivitySection(activity = activity)
            }
        }
    }
}

@Composable
private fun ActivitySection(activity: DiscordActivity) {
    val typeLabel = ActivityType.fromValue(activity.type).label
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = typeLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Large image
            if (activity.assets?.largeImage != null) {
                Box(
                    modifier = Modifier.size(60.dp),
                ) {
                    AsyncImage(
                        model = activity.assets.largeImage.removePrefix("mp:").let { "https://media.discordapp.net/$it" },
                        contentDescription = activity.assets.largeText,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    if (activity.assets.smallImage != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .clip(CircleShape),
                        ) {
                            AsyncImage(
                                model = activity.assets.smallImage.removePrefix("mp:").let { "https://media.discordapp.net/$it" },
                                contentDescription = activity.assets.smallText,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            } else {
                LargeImagePlaceholder(null)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name.ifBlank { "Activity" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                activity.details?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                activity.state?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                activity.timestamps?.start?.let { start ->
                    val elapsed = (System.currentTimeMillis() - start) / 1000
                    val h = elapsed / 3600; val m = (elapsed % 3600) / 60; val s = elapsed % 60
                    val label = if (h > 0) "%02d:%02d:%02d elapsed".format(h, m, s) else "%02d:%02d elapsed".format(m, s)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }

        // Buttons
        if (!activity.buttons.isNullOrEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                activity.buttons.take(2).forEach { btn ->
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Text(btn, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeImagePlaceholder(text: String?) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text?.take(2)?.uppercase() ?: "??",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusChipSmall(status: String) {
    val lyraColors = LocalLyraColors.current
    val (color, label) = when (status) {
        "online"    -> lyraColors.statusOnline to "Online"
        "idle"      -> lyraColors.statusIdle to "Idle"
        "dnd"       -> lyraColors.statusDnd to "DND"
        "invisible" -> lyraColors.statusInvisible to "Invisible"
        else        -> lyraColors.statusInvisible to "Offline"
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

package com.lyra.aura.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.lyra.aura.ui.theme.*

// ── Glass Card ─────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    innerPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val lyraColors = LocalLyraColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val pulse by rememberInfiniteTransition(label = "liquid-card-pulse").animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liquid-card-highlight",
    )

    val baseModifier = modifier
        .shadow(18.dp, shape = shape, ambientColor = lyraColors.shimmer.copy(alpha = 0.18f), spotColor = lyraColors.shimmer.copy(alpha = 0.22f))
        .drawBehind { drawGlassBackground(lyraColors, shape, size, pulse) }
        .border(
            // Use a solid border instead of a diagonal linearGradient.  The previous
            // implementation used Float.MAX_VALUE as the gradient end point, which
            // caused a native crash on some devices (e.g. Android 16) when the
            // shader attempted to create an infinitely large gradient.  A simple
            // solid border preserves the glass effect without provoking crashes.
            border = BorderStroke(
                width = 1.dp,
                brush = SolidColor(lyraColors.glassBorder.copy(alpha = 0.5f)),
            ),
            shape = shape,
        )
        .clip(shape)

    if (onClick != null) {
        Box(
            modifier = baseModifier.clickable(onClick = onClick),
        ) {
            Column(modifier = Modifier.padding(innerPadding), content = content)
        }
    } else {
        Box(modifier = baseModifier) {
            Column(modifier = Modifier.padding(innerPadding), content = content)
        }
    }
}

private fun DrawScope.drawGlassBackground(lyraColors: LyraColors, shape: RoundedCornerShape, size: androidx.compose.ui.geometry.Size, pulse: Float = 1f) {
    val cornerPx = shape.topStart.toPx(size, this)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                lyraColors.glassHighlight.copy(alpha = (lyraColors.glassHighlight.alpha * pulse).coerceIn(0f, 1f)),
                lyraColors.glassTint.copy(alpha = (lyraColors.glassTint.alpha * 1.15f).coerceIn(0f, 1f)),
                lyraColors.glassTint.copy(alpha = (lyraColors.glassTint.alpha * 0.45f).coerceIn(0f, 1f)),
            ),
            start = Offset(0f, 0f),
            end   = Offset(size.width, size.height),
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx),
    )
}

// ── Glass Surface (larger, elevated) ──────────────────────────────────────

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit,
) {
    val lyraColors = LocalLyraColors.current
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(24.dp, shape = shape, ambientColor = lyraColors.shimmer.copy(alpha = 0.16f), spotColor = lyraColors.shimmer.copy(alpha = 0.20f))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lyraColors.glassTint.copy(alpha = 0.14f),
                        lyraColors.glassTint.copy(alpha = 0.06f),
                    )
                ),
                shape = shape,
            )
            .border(
                // Use a solid color for the border.  See GlassCard for details about
                // avoiding crashes caused by extremely large gradients on certain
                // devices.
                BorderStroke(
                    1.dp,
                    SolidColor(lyraColors.glassBorder.copy(alpha = 0.3f)),
                ),
                shape,
            )
            .clip(shape)
    ) { content() }
}

// ── Lyra Text Field ────────────────────────────────────────────────────────

@Composable
fun LyraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLength: Int? = null,
    showCharCount: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { new ->
                if (maxLength == null || new.length <= maxLength) onValueChange(new)
            },
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }} else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedLabelColor     = MaterialTheme.colorScheme.primary,
            ),
        )
        if (maxLength != null && showCharCount) {
            Text(
                text  = "${value.length}/$maxLength",
                style = MaterialTheme.typography.labelSmall,
                color = if (value.length >= maxLength) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp, end = 4.dp),
            )
        }
        if (supportingText != null) {
            Text(
                text  = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        action?.invoke()
    }
}

// ── Lyra Switch Row ────────────────────────────────────────────────────────

@Composable
fun LyraSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        innerPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        onClick = if (enabled) {{ onCheckedChange(!checked) }} else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(end = 14.dp)) { icon() }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor  = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor  = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
    }
}

// ── Connection Status Pill ─────────────────────────────────────────────────

@Composable
fun ConnectionPill(
    isConnected: Boolean,
    isConnecting: Boolean,
    latencyMs: Long = 0,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    val (color, label) = when {
        isConnected  -> MaterialTheme.colorScheme.primary to if (latencyMs > 0) "${latencyMs}ms" else "Connected"
        isConnecting -> StatusIdle to "Connecting…"
        else         -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) to "Disconnected"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isConnecting) color.copy(alpha = pulse) else color,
                        shape = CircleShape,
                    )
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Status Dot ─────────────────────────────────────────────────────────────

@Composable
fun StatusDot(status: String, size: Dp = 12.dp) {
    val lyraColors = LocalLyraColors.current
    val color = when (status) {
        "online"    -> lyraColors.statusOnline
        "idle"      -> lyraColors.statusIdle
        "dnd"       -> lyraColors.statusDnd
        "invisible" -> lyraColors.statusInvisible
        else        -> lyraColors.statusInvisible
    }
    Box(modifier = Modifier.size(size).background(color, CircleShape))
}

// ── Lyra Chip ──────────────────────────────────────────────────────────────

@Composable
fun LyraChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            icon?.invoke()
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// ── Warning Banner ─────────────────────────────────────────────────────────

@Composable
fun WarningBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { Icon(Icons.Default.Warning, null, tint = LyraWarning) },
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LyraWarning.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, LyraWarning.copy(alpha = 0.3f)),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            icon()
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = LyraWarning, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────────────

@Composable
fun EmptyState(icon: @Composable () -> Unit, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

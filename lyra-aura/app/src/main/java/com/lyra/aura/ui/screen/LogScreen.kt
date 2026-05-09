package com.lyra.aura.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.ui.theme.LyraError
import com.lyra.aura.ui.theme.LyraSuccess
import com.lyra.aura.ui.theme.LyraWarning
import com.lyra.aura.viewmodel.MainViewModel

@Composable
fun LogScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val log = mainViewModel.gatewayLog.collectAsState(initial = "").value
    val logLines = remember(log) { log.split("\n").filter { it.isNotBlank() } }
    val listState = rememberLazyListState()
    val connectionState by mainViewModel.connectionState.collectAsState()

    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) listState.animateScrollToItem(logLines.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().background(LavenderBg)) {
        SmallTopAppBar(
            title = { Text("Gateway Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = LavenderBg),
        )

        if (logLines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                    Text("No log entries yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Connect to Discord to see gateway activity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF080613).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(logLines) { line ->
                        val (color, prefix) = when {
                            line.contains("error", ignoreCase = true)   || line.contains("failure", ignoreCase = true) ->
                                LyraError to "✗ "
                            line.contains("READY") || line.contains("Connected") || line.contains("ACK") ->
                                LyraSuccess to "✓ "
                            line.contains("Heartbeat") || line.contains("heartbeat") ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) to "♡ "
                            line.contains("Reconnect") || line.contains("reconnect") ->
                                LyraWarning to "⟳ "
                            else ->
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) to "  "
                        }
                        Text(
                            text = prefix + line,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        )
                    }
                }
            }
        }
    }
}

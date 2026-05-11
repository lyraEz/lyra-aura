package com.lyra.aura.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.lyra.aura.ui.components.GlassCard
import com.lyra.aura.ui.components.WarningBanner
import com.lyra.aura.ui.theme.*
import com.lyra.aura.viewmodel.MainViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
) {
    var showWebView by remember { mutableStateOf(false) }
    val hasToken = mainViewModel.hasToken()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        LavenderSecondary.copy(alpha = 0.15f),
                        LavenderBg,
                    ),
                    radius = 800f,
                )
            )
    ) {
        if (showWebView) {
            Column(modifier = Modifier.fillMaxSize()) {
                // WebView header
                Surface(
                    color = LavenderSurface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        IconButton(onClick = { showWebView = false }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                        Text("Discord Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Actual WebView
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(0.dp)),
                    factory = { ctx ->
                        // Configure a WebView capable of rendering Discord's login page. Discord's site
                        // relies heavily on JavaScript, local storage and cookies. Without these flags
                        // the WebView may render a blank white screen instead of the login form.
                        val webView = WebView(ctx)

                        webView.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString =
                                "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}; " +
                                "${android.os.Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        // Android WebView disables third-party cookies by default; Discord login needs
                        // cookie/session support, so enable it for this WebView instance.
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(webView, true)
                        }

                        webView.webChromeClient = object : WebChromeClient() {}
                        webView.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = false
                        }

                        webView.loadUrl("https://discord.com/login")
                        webView
                    },
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 64.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Back
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Logo / icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                listOf(LavenderPrimary.copy(alpha = 0.3f), LavenderSecondary.copy(alpha = 0.1f))
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape,
                        )
                        .border(2.dp, LavenderPrimary.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✨", style = MaterialTheme.typography.displaySmall)
                }

                Text("Lyra Aura", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Liquid Glass client for Discord Rich Presence experiments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                // Status
                if (hasToken) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = LyraSuccess, modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Token detected", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LyraSuccess)
                                Text("You appear to be logged in. Go back and connect.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { mainViewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Logout / Switch Account")
                    }
                } else {
                    Button(
                        onClick = { showWebView = true },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                    ) {
                        Text("Login with Discord WebView", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/login")))
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Abrir Discord no navegador")
                    }
                }

                Spacer(Modifier.height(8.dp))

                WarningBanner(
                    title = "Security Notice",
                    body  = "Your Discord token is stored only on this device. Lyra Aura never transmits it to any external server. The token is used solely to connect to Discord's Gateway API.\n\nUsing user tokens violates Discord's ToS. This is for educational purposes only.",
                )

                // How it works
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("How Login Works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    listOf(
                        "1." to "You log in via Discord's official website in a WebView",
                        "2." to "The app reads the token from the browser's local storage",
                        "3." to "The token is used to connect to Discord's Gateway WebSocket",
                        "4." to "No credentials are sent anywhere except to Discord",
                    ).forEach { (num, text) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(num, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Button(
                    onClick = { showWebView = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Discord Login")
                }
            }
        }
    }
}

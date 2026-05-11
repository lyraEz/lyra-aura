
package com.lyra.aura.finance

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class Tx(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val amount: Double,
    val date: String
)

data class Goal(
    val name: String,
    val target: Double,
    val saved: Double,
    val months: Int
)

data class Holding(
    val ticker: String,
    val kind: String,
    val quantity: Double,
    val avgPrice: Double,
    val currentPrice: Double
)

data class MarketQuote(
    val label: String,
    val value: String,
    val change: String = "",
    val source: String = "local"
)

enum class AuraTheme(val label: String) {
    Midnight("Preto neon"),
    Snow("Branco vidro"),
    Caramel("Caramelo"),
    Lavender("Lavanda"),
    Custom("Manual")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AuraFinanceApp() }
    }
}

@Composable
fun AuraFinanceApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("aura_finance", Context.MODE_PRIVATE) }

    var logged by remember { mutableStateOf(prefs.getBoolean("logged", false)) }
    var theme by remember { mutableStateOf(AuraTheme.valueOf(prefs.getString("theme", AuraTheme.Midnight.name) ?: AuraTheme.Midnight.name)) }
    var customHex by remember { mutableStateOf(prefs.getString("customHex", "#B78CFF") ?: "#B78CFF") }
    var animations by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
    var glass by remember { mutableStateOf(prefs.getFloat("glass", 0.72f)) }

    val palette = remember(theme, customHex) { paletteOf(theme, customHex) }

    MaterialTheme(
        colorScheme = if (palette.dark) darkColorScheme(
            primary = palette.accent,
            background = palette.bg,
            surface = palette.surface,
            onSurface = palette.text,
            onBackground = palette.text
        ) else lightColorScheme(
            primary = palette.accent,
            background = palette.bg,
            surface = palette.surface,
            onSurface = palette.text,
            onBackground = palette.text
        ),
        shapes = Shapes(
            small = RoundedCornerShape(18.dp),
            medium = RoundedCornerShape(26.dp),
            large = RoundedCornerShape(34.dp),
            extraLarge = RoundedCornerShape(42.dp)
        )
    ) {
        if (!logged) {
            AuthScreen(
                palette = palette,
                animations = animations,
                onLogged = {
                    prefs.edit().putBoolean("logged", true).apply()
                    logged = true
                }
            )
        } else {
            FinanceHome(
                palette = palette,
                prefs = prefs,
                theme = theme,
                onTheme = {
                    theme = it
                    prefs.edit().putString("theme", it.name).apply()
                },
                customHex = customHex,
                onCustomHex = {
                    customHex = it
                    prefs.edit().putString("customHex", it).apply()
                },
                animations = animations,
                onAnimations = {
                    animations = it
                    prefs.edit().putBoolean("animations", it).apply()
                },
                glass = glass,
                onGlass = {
                    glass = it
                    prefs.edit().putFloat("glass", it).apply()
                },
                onLogout = {
                    prefs.edit().putBoolean("logged", false).apply()
                    logged = false
                }
            )
        }
    }
}

data class AuraPalette(
    val bg: Color,
    val bg2: Color,
    val surface: Color,
    val glass: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accent2: Color,
    val good: Color,
    val bad: Color,
    val warn: Color,
    val dark: Boolean
)

fun paletteOf(theme: AuraTheme, customHex: String): AuraPalette {
    fun parse(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFFB78CFF))
    val custom = parse(customHex)
    return when (theme) {
        AuraTheme.Snow -> AuraPalette(
            bg = Color(0xFFF8F7FF), bg2 = Color(0xFFECE7FF), surface = Color.White,
            glass = Color.White.copy(alpha = 0.76f), text = Color(0xFF151020), muted = Color(0xFF5E5574),
            accent = Color(0xFF6D45FF), accent2 = Color(0xFFFF7AB6),
            good = Color(0xFF0C9B64), bad = Color(0xFFE3365C), warn = Color(0xFFBE7200), dark = false
        )
        AuraTheme.Caramel -> AuraPalette(
            bg = Color(0xFF160C05), bg2 = Color(0xFF3D220D), surface = Color(0xFF241306),
            glass = Color(0xFFFFC47A).copy(alpha = 0.16f), text = Color(0xFFFFEFD9), muted = Color(0xFFE0B889),
            accent = Color(0xFFFFB35C), accent2 = Color(0xFFFF7043),
            good = Color(0xFF8AFFC1), bad = Color(0xFFFF6D8A), warn = Color(0xFFFFD166), dark = true
        )
        AuraTheme.Lavender -> AuraPalette(
            bg = Color(0xFF070312), bg2 = Color(0xFF211044), surface = Color(0xFF130A2A),
            glass = Color(0xFFC8A2FF).copy(alpha = 0.20f), text = Color(0xFFF1E8FF), muted = Color(0xFFC8B7EA),
            accent = Color(0xFFB78CFF), accent2 = Color(0xFFFF7AC7),
            good = Color(0xFF57F287), bad = Color(0xFFFF5C8A), warn = Color(0xFFFFD166), dark = true
        )
        AuraTheme.Custom -> AuraPalette(
            bg = Color(0xFF07070B), bg2 = custom.copy(alpha = 0.45f), surface = Color(0xFF111119),
            glass = custom.copy(alpha = 0.20f), text = Color(0xFFF4F2FF), muted = Color(0xFFBDB8D6),
            accent = custom, accent2 = Color(0xFFFF7AB6),
            good = Color(0xFF57F287), bad = Color(0xFFFF5C8A), warn = Color(0xFFFFD166), dark = true
        )
        else -> AuraPalette(
            bg = Color(0xFF05050A), bg2 = Color(0xFF101025), surface = Color(0xFF0B0B14),
            glass = Color(0xFFBBD7FF).copy(alpha = 0.13f), text = Color(0xFFF7F7FF), muted = Color(0xFFB1B1C8),
            accent = Color(0xFF9DD7FF), accent2 = Color(0xFFB78CFF),
            good = Color(0xFF57F287), bad = Color(0xFFFF5C8A), warn = Color(0xFFFFD166), dark = true
        )
    }
}

@Composable
fun AuthScreen(palette: AuraPalette, animations: Boolean, onLogged: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("aura_finance", Context.MODE_PRIVATE) }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(prefs.getString("name", "Lyra") ?: "Lyra") }
    var createMode by remember { mutableStateOf(prefs.getString("userHash", null) == null) }
    var msg by remember { mutableStateOf("") }

    LiquidBackground(palette, animations) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedHalo(palette, animations)
            Spacer(Modifier.height(20.dp))
            Text("Aura Finance", fontSize = 34.sp, fontWeight = FontWeight.Black, color = palette.text)
            Text(
                "finanças, metas, investimentos e mercado em uma interface glass absurda",
                color = palette.muted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text(if (createMode) "Criar acesso local" else "Entrar no app", color = palette.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (createMode) {
                    LiquidField("Seu nome", name, { name = it }, palette)
                    Spacer(Modifier.height(10.dp))
                }
                LiquidField("E-mail do app", email, { email = it }, palette)
                Spacer(Modifier.height(10.dp))
                LiquidField("Senha do app", pass, { pass = it }, palette, password = true)
                Spacer(Modifier.height(16.dp))
                LiquidButton(
                    label = if (createMode) "Criar conta local" else "Entrar",
                    palette = palette,
                    animations = animations,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (email.isBlank() || pass.length < 4) {
                        msg = "Coloca um e-mail e uma senha com pelo menos 4 caracteres."
                    } else if (createMode) {
                        prefs.edit()
                            .putString("email", email.trim())
                            .putString("name", name.ifBlank { "Usuário" })
                            .putString("userHash", sha256(email.trim().lowercase() + ":" + pass))
                            .apply()
                        onLogged()
                    } else {
                        val expected = prefs.getString("userHash", "")
                        val ok = expected == sha256(email.trim().lowercase() + ":" + pass)
                        if (ok) onLogged() else msg = "Login local inválido. Senha errada, clássico plot humano."
                    }
                }
                TextButton(onClick = { createMode = !createMode }) {
                    Text(if (createMode) "Já tenho conta local" else "Criar nova conta local", color = palette.accent)
                }
                if (msg.isNotBlank()) Text(msg, color = palette.warn, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FinanceHome(
    palette: AuraPalette,
    prefs: android.content.SharedPreferences,
    theme: AuraTheme,
    onTheme: (AuraTheme) -> Unit,
    customHex: String,
    onCustomHex: (String) -> Unit,
    animations: Boolean,
    onAnimations: (Boolean) -> Unit,
    glass: Float,
    onGlass: (Float) -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val txs = remember { mutableStateListOf<Tx>().apply { addAll(loadTransactions(prefs)) } }
    val goals = remember { mutableStateListOf<Goal>().apply { addAll(loadGoals(prefs)) } }
    val holdings = remember { mutableStateListOf<Holding>().apply { addAll(loadHoldings(prefs)) } }

    fun saveAll() {
        saveTransactions(prefs, txs)
        saveGoals(prefs, goals)
        saveHoldings(prefs, holdings)
    }

    LiquidBackground(palette, animations) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                LiquidNav(tab, { tab = it }, palette, animations)
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInHorizontally { it / 8 }) togetherWith
                                (fadeOut(tween(160)) + slideOutHorizontally { -it / 10 })
                    },
                    label = "screen-switch"
                ) { current ->
                    when (current) {
                        0 -> DashboardScreen(palette, animations, txs, goals, holdings)
                        1 -> TransactionsScreen(palette, animations, txs, onSave = { saveAll() })
                        2 -> CalculatorsScreen(palette, animations)
                        3 -> MarketScreen(palette, animations, holdings, onSave = { saveAll() })
                        else -> SettingsScreen(palette, animations, theme, onTheme, customHex, onCustomHex, glass, onGlass, txs, goals, holdings, onLogout) { saveAll() }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(palette: AuraPalette, animations: Boolean, txs: List<Tx>, goals: List<Goal>, holdings: List<Holding>) {
    val income = txs.filter { it.type == "Ganho" }.sumOf { it.amount }
    val expense = txs.filter { it.type == "Gasto" }.sumOf { it.amount }
    val invested = holdings.sumOf { it.quantity * it.currentPrice }
    val cost = holdings.sumOf { it.quantity * it.avgPrice }
    val balance = income - expense
    val savingsRate = if (income > 0) ((income - expense) / income * 100).coerceAtLeast(0.0) else 0.0
    val dailyLimit = max(0.0, balance / 30.0)

    LazyColumn(
        Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Header("Aura Finance", "Dashboard líquido", palette)
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Patrimônio estimado", color = palette.muted)
                Text(money(balance + invested), color = palette.text, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("Saldo", money(balance), palette.good, palette, Modifier.weight(1f))
                    StatPill("Investido", money(invested), palette.accent, palette, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("Gastos", money(expense), palette.bad, palette, Modifier.weight(1f))
                    StatPill("Limite/dia", money(dailyLimit), palette.warn, palette, Modifier.weight(1f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricRing("Economia", savingsRate / 100.0, "${savingsRate.toInt()}%", palette.good, palette, Modifier.weight(1f))
                MetricRing("Carteira", if (cost > 0) ((invested - cost) / cost + 1).coerceIn(0.0, 2.0) / 2.0 else 0.5, if (cost > 0) "${(((invested - cost) / cost) * 100).toInt()}%" else "0%", palette.accent, palette, Modifier.weight(1f))
            }
        }
        item { FeatureGrid(palette, animations) }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Metas", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (goals.isEmpty()) Text("Sem metas ainda. O cofrinho está em modo fantasma.", color = palette.muted)
                goals.take(3).forEach { g ->
                    Spacer(Modifier.height(10.dp))
                    val p = if (g.target > 0) (g.saved / g.target).coerceIn(0.0, 1.0) else 0.0
                    Text(g.name, color = palette.text, fontWeight = FontWeight.SemiBold)
                    LiquidProgress(p, palette)
                    Text("${money(g.saved)} / ${money(g.target)} • ${money(max(0.0, (g.target - g.saved) / max(1, g.months).toDouble()))}/mês", color = palette.muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TransactionsScreen(palette: AuraPalette, animations: Boolean, txs: SnapshotStateList<Tx>, onSave: () -> Unit) {
    var type by remember { mutableStateOf("Gasto") }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Geral") }
    var filter by remember { mutableStateOf("Todos") }
    val categories = listOf("Geral", "Comida", "Transporte", "Casa", "Estudo", "Lazer", "Saúde", "Investimento", "Assinaturas")
    val visible = txs.filter { filter == "Todos" || it.type == filter }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header("Gastos e ganhos", "controle local, rápido e sem planilha do inferno", palette) }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChoiceChip("Gasto", type == "Gasto", { type = "Gasto" }, palette)
                    ChoiceChip("Ganho", type == "Ganho", { type = "Ganho" }, palette)
                }
                Spacer(Modifier.height(10.dp))
                LiquidField("Título", title, { title = it }, palette)
                Spacer(Modifier.height(10.dp))
                LiquidField("Valor", amount, { amount = it.onlyMoney() }, palette)
                Spacer(Modifier.height(10.dp))
                ScrollChips(categories, category, { category = it }, palette)
                Spacer(Modifier.height(14.dp))
                LiquidButton("Adicionar", palette, animations, Modifier.fillMaxWidth()) {
                    val v = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && v > 0) {
                        txs.add(0, Tx(System.currentTimeMillis(), type, category, title, v, today()))
                        title = ""; amount = ""
                        onSave()
                    }
                }
            }
        }
        item {
            ScrollChips(listOf("Todos", "Gasto", "Ganho"), filter, { filter = it }, palette)
        }
        itemsIndexed(visible) { _, tx ->
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (tx.type == "Ganho") Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (tx.type == "Ganho") palette.good else palette.bad)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tx.title, color = palette.text, fontWeight = FontWeight.Bold)
                        Text("${tx.category} • ${tx.date}", color = palette.muted, fontSize = 12.sp)
                    }
                    Text((if (tx.type == "Ganho") "+" else "-") + money(tx.amount), color = if (tx.type == "Ganho") palette.good else palette.bad, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { txs.remove(tx); onSave() }) {
                        Icon(Icons.Default.Delete, null, tint = palette.muted)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorsScreen(palette: AuraPalette, animations: Boolean) {
    var principal by remember { mutableStateOf("1000") }
    var monthly by remember { mutableStateOf("200") }
    var rate by remember { mutableStateOf("10") }
    var months by remember { mutableStateOf("24") }
    var inflation by remember { mutableStateOf("4.5") }
    var loanRate by remember { mutableStateOf("2.2") }
    var loanMonths by remember { mutableStateOf("24") }
    var risk by remember { mutableStateOf("Médio") }
    var horizon by remember { mutableStateOf("24") }
    var liquidity by remember { mutableStateOf("Média") }

    val p = principal.toDoubleOrNull() ?: 0.0
    val m = monthly.toDoubleOrNull() ?: 0.0
    val r = (rate.toDoubleOrNull() ?: 0.0) / 100.0 / 12.0
    val n = months.toIntOrNull() ?: 0
    val future = if (r == 0.0) p + m * n else p * (1 + r).pow(n) + m * (((1 + r).pow(n) - 1) / r)
    val realReturn = (1 + (rate.toDoubleOrNull() ?: 0.0) / 100.0) / (1 + (inflation.toDoubleOrNull() ?: 0.0) / 100.0) - 1
    val loan = p
    val loanMonthlyRate = (loanRate.toDoubleOrNull() ?: 0.0) / 100.0
    val loanN = loanMonths.toIntOrNull() ?: 1
    val payment = if (loanMonthlyRate == 0.0) loan / loanN else loan * (loanMonthlyRate * (1 + loanMonthlyRate).pow(loanN)) / ((1 + loanMonthlyRate).pow(loanN) - 1)

    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header("Calculadoras", "juros, IPCA, dívidas, metas e investimentos", palette) }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Juros compostos", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(10.dp))
                LiquidField("Inicial", principal, { principal = it.onlyMoney() }, palette)
                LiquidField("Aporte mensal", monthly, { monthly = it.onlyMoney() }, palette)
                LiquidField("Rentabilidade ao ano (%)", rate, { rate = it.onlyMoney() }, palette)
                LiquidField("Meses", months, { months = it.filter(Char::isDigit) }, palette)
                Spacer(Modifier.height(10.dp))
                BigResult("Valor futuro", money(future), palette)
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Retorno real vs IPCA", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                LiquidField("Inflação/IPCA ao ano (%)", inflation, { inflation = it.onlyMoney() }, palette)
                BigResult("Ganho real estimado", "${(realReturn * 100).format(2)}% a.a.", palette)
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Financiamento / empréstimo", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                LiquidField("Valor financiado", principal, { principal = it.onlyMoney() }, palette)
                LiquidField("Juros ao mês (%)", loanRate, { loanRate = it.onlyMoney() }, palette)
                LiquidField("Parcelas", loanMonths, { loanMonths = it.filter(Char::isDigit) }, palette)
                BigResult("Parcela estimada", money(payment), palette)
                Text("CET, impostos e tarifas não entram nessa conta. Banco sempre inventa uma taxa com nome de feitiço.", color = palette.muted, fontSize = 12.sp)
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Radar de investimento educacional", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                ScrollChips(listOf("Baixo", "Médio", "Alto"), risk, { risk = it }, palette)
                LiquidField("Horizonte em meses", horizon, { horizon = it.filter(Char::isDigit) }, palette)
                ScrollChips(listOf("Alta", "Média", "Baixa"), liquidity, { liquidity = it }, palette)
                Spacer(Modifier.height(12.dp))
                investmentRanking(risk, horizon.toIntOrNull() ?: 0, liquidity).forEach { item ->
                    Text("• $item", color = palette.text)
                }
                Text("Isso é triagem educativa, não recomendação financeira. Dinheiro real exige estudo real, infelizmente.", color = palette.warn, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MarketScreen(palette: AuraPalette, animations: Boolean, holdings: SnapshotStateList<Holding>, onSave: () -> Unit) {
    val scope = rememberCoroutineScope()
    var quotes by remember { mutableStateOf(listOf<MarketQuote>()) }
    var loading by remember { mutableStateOf(false) }
    var ticker by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("Ação/FII") }
    var qty by remember { mutableStateOf("") }
    var avg by remember { mutableStateOf("") }
    var cur by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            loading = true
            quotes = fetchMarket()
            loading = false
        }
    }
    LaunchedEffect(Unit) { refresh() }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header("Mercado ao vivo", "moedas, Selic/IPCA e carteira manual", palette) }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cotações", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { refresh() }) { Icon(Icons.Default.Refresh, null, tint = palette.accent) }
                }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = palette.accent)
                Spacer(Modifier.height(8.dp))
                quotes.ifEmpty { listOf(MarketQuote("Sem conexão", "use os cálculos offline", "", "local")) }.forEach {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(it.label, color = palette.text, fontWeight = FontWeight.SemiBold)
                            Text(it.source, color = palette.muted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(it.value, color = palette.accent, fontWeight = FontWeight.Bold)
                            if (it.change.isNotBlank()) Text(it.change, color = if (it.change.startsWith("-")) palette.bad else palette.good, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Carteira manual", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                LiquidField("Ticker", ticker, { ticker = it.uppercase() }, palette)
                ScrollChips(listOf("Ação/FII", "Renda fixa", "Cripto", "Fundo", "Caixa"), kind, { kind = it }, palette)
                LiquidField("Quantidade", qty, { qty = it.onlyMoney() }, palette)
                LiquidField("Preço médio", avg, { avg = it.onlyMoney() }, palette)
                LiquidField("Preço atual", cur, { cur = it.onlyMoney() }, palette)
                LiquidButton("Adicionar ativo", palette, animations, Modifier.fillMaxWidth()) {
                    val q = qty.toDoubleOrNull() ?: 0.0
                    val a = avg.toDoubleOrNull() ?: 0.0
                    val c = cur.toDoubleOrNull() ?: 0.0
                    if (ticker.isNotBlank() && q > 0 && c >= 0) {
                        holdings.add(Holding(ticker, kind, q, a, c))
                        ticker = ""; qty = ""; avg = ""; cur = ""
                        onSave()
                    }
                }
            }
        }
        itemsIndexed(holdings) { _, h ->
            val pnl = (h.currentPrice - h.avgPrice) * h.quantity
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(h.ticker, color = palette.text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("${h.kind} • ${h.quantity.format(2)} un.", color = palette.muted, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(money(h.quantity * h.currentPrice), color = palette.accent, fontWeight = FontWeight.Bold)
                        Text((if (pnl >= 0) "+" else "") + money(pnl), color = if (pnl >= 0) palette.good else palette.bad, fontSize = 12.sp)
                    }
                    IconButton(onClick = { holdings.remove(h); onSave() }) {
                        Icon(Icons.Default.Delete, null, tint = palette.muted)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    palette: AuraPalette,
    animations: Boolean,
    theme: AuraTheme,
    onTheme: (AuraTheme) -> Unit,
    customHex: String,
    onCustomHex: (String) -> Unit,
    glass: Float,
    onGlass: (Float) -> Unit,
    txs: SnapshotStateList<Tx>,
    goals: SnapshotStateList<Goal>,
    holdings: SnapshotStateList<Holding>,
    onLogout: () -> Unit,
    onSave: () -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalSaved by remember { mutableStateOf("") }
    var goalMonths by remember { mutableStateOf("12") }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header("Configurações", "tema, metas, backup mental e botões bonitos", palette) }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Temas", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                ScrollChips(AuraTheme.entries.map { it.label }, theme.label, { label -> AuraTheme.entries.find { it.label == label }?.let(onTheme) }, palette)
                LiquidField("Cor manual HEX", customHex, onCustomHex, palette)
                Text("Glass strength ${(glass * 100).toInt()}%", color = palette.muted)
                Slider(value = glass, onValueChange = onGlass, valueRange = 0.35f..1f, colors = SliderDefaults.colors(thumbColor = palette.accent, activeTrackColor = palette.accent))
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Metas financeiras", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                LiquidField("Nome da meta", goalName, { goalName = it }, palette)
                LiquidField("Valor alvo", goalTarget, { goalTarget = it.onlyMoney() }, palette)
                LiquidField("Já guardado", goalSaved, { goalSaved = it.onlyMoney() }, palette)
                LiquidField("Prazo em meses", goalMonths, { goalMonths = it.filter(Char::isDigit) }, palette)
                LiquidButton("Criar meta", palette, animations, Modifier.fillMaxWidth()) {
                    val t = goalTarget.toDoubleOrNull() ?: 0.0
                    if (goalName.isNotBlank() && t > 0) {
                        goals.add(Goal(goalName, t, goalSaved.toDoubleOrNull() ?: 0.0, max(1, goalMonths.toIntOrNull() ?: 1)))
                        goalName = ""; goalTarget = ""; goalSaved = ""
                        onSave()
                    }
                }
            }
        }
        item {
            LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
                Text("Dados locais", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${txs.size} transações • ${goals.size} metas • ${holdings.size} ativos", color = palette.muted)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { txs.clear(); goals.clear(); holdings.clear(); onSave() }, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CleaningServices, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Limpar dados financeiros")
                }
                OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sair")
                }
            }
        }
    }
}

@Composable
fun LiquidBackground(palette: AuraPalette, animations: Boolean, content: @Composable () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "bg")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animations) 1f else 0.001f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(palette.bg, palette.bg2, palette.bg),
                start = Offset(0f, 0f),
                end = Offset(900f + drift * 300f, 1600f)
            )
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(palette.accent.copy(alpha = 0.22f), radius = 360f, center = Offset(size.width * (0.15f + drift * 0.18f), size.height * 0.18f))
            drawCircle(palette.accent2.copy(alpha = 0.18f), radius = 300f, center = Offset(size.width * 0.92f, size.height * (0.25f + drift * 0.18f)))
            drawCircle(Color.White.copy(alpha = if (palette.dark) 0.05f else 0.25f), radius = 500f, center = Offset(size.width * 0.5f, size.height * 1.08f))
        }
        content()
    }
}

@Composable
fun LiquidCard(palette: AuraPalette, animations: Boolean, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val pulse by rememberInfiniteTransition(label = "card").animateFloat(
        0.75f, if (animations) 1f else 0.76f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(
        modifier
            .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = palette.accent.copy(alpha = 0.17f), spotColor = palette.accent.copy(alpha = 0.22f))
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (palette.dark) 0.16f * pulse else 0.72f),
                        palette.glass.copy(alpha = if (palette.dark) 0.30f else 0.58f),
                        palette.surface.copy(alpha = if (palette.dark) 0.58f else 0.62f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = if (palette.dark) 0.20f else 0.55f), RoundedCornerShape(32.dp))
            .padding(18.dp),
        content = content
    )
}

@Composable
fun LiquidButton(label: String, palette: AuraPalette, animations: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (animations) 1f else 1f, label = "btn")
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = if (palette.dark) Color(0xFF080611) else Color.White)
    ) { Text(label, fontWeight = FontWeight.Bold) }
}

@Composable
fun LiquidField(label: String, value: String, onChange: (String) -> Unit, palette: AuraPalette, password: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palette.accent,
            unfocusedBorderColor = palette.muted.copy(alpha = 0.35f),
            focusedTextColor = palette.text,
            unfocusedTextColor = palette.text,
            focusedLabelColor = palette.accent,
            unfocusedLabelColor = palette.muted,
            cursorColor = palette.accent,
            focusedContainerColor = Color.White.copy(alpha = if (palette.dark) 0.07f else 0.38f),
            unfocusedContainerColor = Color.White.copy(alpha = if (palette.dark) 0.05f else 0.25f)
        )
    )
}

@Composable
fun Header(title: String, subtitle: String, palette: AuraPalette) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp)) {
        Text(title, color = palette.text, fontWeight = FontWeight.Black, fontSize = 30.sp)
        Text(subtitle, color = palette.muted)
    }
}

@Composable
fun StatPill(label: String, value: String, color: Color, palette: AuraPalette, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(color.copy(alpha = 0.13f)).padding(12.dp)) {
        Text(label, color = palette.muted, fontSize = 12.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricRing(label: String, progress: Double, value: String, color: Color, palette: AuraPalette, modifier: Modifier = Modifier) {
    LiquidCard(palette, true, modifier) {
        Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(92.dp)) {
                drawCircle(palette.muted.copy(alpha = 0.17f), style = Stroke(14f))
                drawArc(color, -90f, (progress.coerceIn(0.0,1.0) * 360).toFloat(), false, style = Stroke(14f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(label, color = palette.muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LiquidProgress(progress: Double, palette: AuraPalette) {
    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(palette.muted.copy(alpha = 0.18f))) {
        Box(Modifier.fillMaxWidth(progress.toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(palette.accent, palette.accent2))))
    }
}

@Composable
fun BigResult(label: String, value: String, palette: AuraPalette) {
    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(palette.accent.copy(alpha = 0.14f)).padding(16.dp)) {
        Column {
            Text(label, color = palette.muted)
            Text(value, color = palette.accent, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ScrollChips(items: List<String>, selected: String, onSelected: (String) -> Unit, palette: AuraPalette) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item -> ChoiceChip(item, selected == item, { onSelected(item) }, palette) }
    }
}

@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, palette: AuraPalette) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(if (selected) palette.accent else Color.White.copy(alpha = if (palette.dark) 0.07f else 0.35f))
            .border(1.dp, if (selected) Color.Transparent else palette.muted.copy(alpha = 0.25f), RoundedCornerShape(50))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(text, color = if (selected) (if (palette.dark) Color(0xFF080611) else Color.White) else palette.text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
fun LiquidNav(tab: Int, setTab: (Int) -> Unit, palette: AuraPalette, animations: Boolean) {
    val items = listOf(
        Icons.Default.Dashboard to "Início",
        Icons.Default.ReceiptLong to "Fluxo",
        Icons.Default.Calculate to "Cálculos",
        Icons.Default.CandlestickChart to "Mercado",
        Icons.Default.Tune to "Config"
    )
    Row(
        Modifier.fillMaxWidth().padding(12.dp).shadow(22.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp))
            .background(palette.glass.copy(alpha = if (palette.dark) 0.58f else 0.82f)).border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(32.dp)).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEachIndexed { i, pair ->
            val selected = tab == i
            val w by animateDpAsState(if (selected) 104.dp else 56.dp, tween(260, easing = FastOutSlowInEasing), label = "navW")
            Row(
                Modifier.height(48.dp).width(w).clip(RoundedCornerShape(24.dp)).background(if (selected) palette.accent else Color.Transparent).clickable { setTab(i) }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(pair.first, null, tint = if (selected) (if (palette.dark) Color(0xFF080611) else Color.White) else palette.muted, modifier = Modifier.size(20.dp))
                AnimatedVisibility(selected) {
                    Text(" ${pair.second}", color = if (palette.dark) Color(0xFF080611) else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AnimatedHalo(palette: AuraPalette, animations: Boolean) {
    val rot by rememberInfiniteTransition(label = "halo").animateFloat(0f, if (animations) 360f else 1f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "rot")
    Box(Modifier.size(118.dp).graphicsLayer { rotationZ = rot }.clip(CircleShape).background(Brush.sweepGradient(listOf(palette.accent, palette.accent2, palette.accent))).border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(palette.bg), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoGraph, null, tint = palette.accent, modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
fun FeatureGrid(palette: AuraPalette, animations: Boolean) {
    val features = listOf(
        "Login local", "Saldo", "Ganhos", "Gastos", "Categorias", "Filtros", "Metas", "Aportes",
        "Juros compostos", "IPCA real", "Empréstimo", "Renda fixa", "Renda variável", "FIIs",
        "Cripto", "Carteira", "P/L manual", "Moedas", "Selic", "Inflação", "Orçamento",
        "Limite diário", "Temas", "HEX manual", "Glass", "Backup local", "Reset", "Dashboard",
        "Animações", "Cotações", "Risco", "Liquidez", "Horizonte", "Score educativo", "Patrimônio",
        "Controle offline"
    )
    LiquidCard(palette, animations, Modifier.fillMaxWidth()) {
        Text("36 funções incluídas", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(10.dp))
        FlowGrid(features, palette)
    }
}

@Composable
fun FlowGrid(items: List<String>, palette: AuraPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(palette.accent.copy(alpha = 0.10f)).padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(it, color = palette.text, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

suspend fun fetchMarket(): List<MarketQuote> = withContext(Dispatchers.IO) {
    val out = mutableListOf<MarketQuote>()
    runCatching {
        val txt = URL("https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL,GBP-BRL,BTC-BRL").readText()
        val json = JSONObject(txt)
        listOf("USDBRL" to "Dólar", "EURBRL" to "Euro", "GBPBRL" to "Libra", "BTCBRL" to "Bitcoin").forEach { (key, label) ->
            val o = json.getJSONObject(key)
            out.add(MarketQuote(label, money(o.optString("bid").toDoubleOrNull() ?: 0.0), o.optString("pctChange") + "%", "AwesomeAPI"))
        }
    }
    runCatching {
        val selic = URL("https://api.bcb.gov.br/dados/serie/bcdata.sgs.11/dados/ultimos/1?formato=json").readText()
        val arr = org.json.JSONArray(selic)
        if (arr.length() > 0) out.add(MarketQuote("Selic diária", arr.getJSONObject(0).optString("valor") + "%", "", "Banco Central SGS"))
    }
    runCatching {
        val ipca = URL("https://api.bcb.gov.br/dados/serie/bcdata.sgs.433/dados/ultimos/1?formato=json").readText()
        val arr = org.json.JSONArray(ipca)
        if (arr.length() > 0) out.add(MarketQuote("IPCA mensal", arr.getJSONObject(0).optString("valor") + "%", "", "Banco Central SGS"))
    }
    out
}

fun investmentRanking(risk: String, horizon: Int, liquidity: String): List<String> {
    val list = mutableListOf<String>()
    if (liquidity == "Alta") list += "Reserva: Tesouro Selic, CDB liquidez diária, fundo DI simples"
    if (risk == "Baixo") list += "Prioridade: renda fixa pós-fixada, IPCA+ curto/médio e caixa"
    if (risk == "Médio") list += "Equilíbrio: IPCA+, CDB/LCI/LCA, FIIs selecionados e pouco índice"
    if (risk == "Alto") list += "Crescimento: ETFs, ações, FIIs e cripto pequena, com volatilidade aceita"
    if (horizon >= 60) list += "Horizonte longo: dá para aceitar mais oscilação em troca de potencial"
    if (horizon < 12) list += "Horizonte curto: fuja de volatilidade grande, liquidez manda"
    list += "Compare sempre taxa líquida, imposto, risco de crédito, liquidez e inflação"
    return list
}

fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(v)
fun Double.format(n: Int) = "%.${n}f".format(Locale.US, this)
fun today(): String = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
fun String.onlyMoney(): String = filter { it.isDigit() || it == '.' || it == ',' }.replace(",", ".")
fun sha256(s: String): String = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

fun loadTransactions(prefs: android.content.SharedPreferences): List<Tx> =
    prefs.getString("txs", "")!!.split("\n").filter { it.contains("|") }.mapNotNull {
        val p = it.split("|")
        runCatching { Tx(p[0].toLong(), p[1], p[2], p[3], p[4].toDouble(), p[5]) }.getOrNull()
    }

fun saveTransactions(prefs: android.content.SharedPreferences, txs: List<Tx>) {
    prefs.edit().putString("txs", txs.joinToString("\n") { "${it.id}|${it.type}|${it.category}|${it.title.replace("|"," ")}|${it.amount}|${it.date}" }).apply()
}

fun loadGoals(prefs: android.content.SharedPreferences): List<Goal> =
    prefs.getString("goals", "")!!.split("\n").filter { it.contains("|") }.mapNotNull {
        val p = it.split("|")
        runCatching { Goal(p[0], p[1].toDouble(), p[2].toDouble(), p[3].toInt()) }.getOrNull()
    }

fun saveGoals(prefs: android.content.SharedPreferences, goals: List<Goal>) {
    prefs.edit().putString("goals", goals.joinToString("\n") { "${it.name.replace("|"," ")}|${it.target}|${it.saved}|${it.months}" }).apply()
}

fun loadHoldings(prefs: android.content.SharedPreferences): List<Holding> =
    prefs.getString("holdings", "")!!.split("\n").filter { it.contains("|") }.mapNotNull {
        val p = it.split("|")
        runCatching { Holding(p[0], p[1], p[2].toDouble(), p[3].toDouble(), p[4].toDouble()) }.getOrNull()
    }

fun saveHoldings(prefs: android.content.SharedPreferences, holdings: List<Holding>) {
    prefs.edit().putString("holdings", holdings.joinToString("\n") { "${it.ticker}|${it.kind}|${it.quantity}|${it.avgPrice}|${it.currentPrice}" }).apply()
}

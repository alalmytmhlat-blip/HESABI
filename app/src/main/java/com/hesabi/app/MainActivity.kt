package com.hesabi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hesabi.app.data.AppDatabase
import com.hesabi.app.data.InitialData
import kotlinx.coroutines.launch

private val HesabiBlue = Color(0xFF0878AC)
private val HesabiBlueDark = Color(0xFF075F8A)
private val HesabiLine = Color(0xFFD9D9D9)
private val HesabiText = Color(0xFF303030)
private val HesabiHeader = Color(0xFFF5F5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HesabiApp() }
    }
}

@Composable
fun HesabiApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()
    var drawerOpen by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf("الرئيسية") }

    LaunchedEffect(Unit) { scope.launch { InitialData(db).seed() } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = HesabiBlue,
                background = Color.White,
                surface = Color.White,
                onSurface = HesabiText
            )
        ) {
            Box(Modifier.fillMaxSize().background(Color.White)) {
                when (screen) {
                    "الأصناف" -> ProductsScreen { screen = "الرئيسية" }
                    "حركة الأصناف" -> StockMovementScreen { screen = "الرئيسية" }
                    "تقارير أخرى" -> ReportsScreen { screen = "الرئيسية" }
                    "الإعدادات" -> SettingsScreen { screen = "الرئيسية" }
                    "الدليل" -> AccountsScreen { screen = "الرئيسية" }
                    "العملاء" -> SimpleListScreen("العملاء", listOf("كشف حساب عميل","إضافة عميل","حركة العملاء","أرصدة العملاء")) { screen = "الرئيسية" }
                    "الموردون" -> SimpleListScreen("الموردون", listOf("كشف حساب مورد","إضافة مورد","حركة الموردين","أرصدة الموردين")) { screen = "الرئيسية" }
                    "المبيعات", "المشتريات", "قبض/صرف" -> TransactionMenuScreen(screen) { screen = "الرئيسية" }
                    "عمليات مخزنية" -> StockOperationsScreen { screen = "الرئيسية" }
                    "قيود وحسابات" -> AccountingOperationsScreen { screen = "الرئيسية" }
                    "العملات" -> CurrenciesScreen { screen = "الرئيسية" }
                    "فاتورة مبيعات", "مرتجع مبيعات", "عرض سعر",
                    "فاتورة مشتريات", "مرتجع مشتريات", "طلبية شراء",
                    "قبض", "صرف", "قيد يومي", "قيد افتتاحي",
                    "صرف مخزني", "توريد مخزني", "تحويل مخزني", "تسوية مخزنية",
                    "إضافة معدن", "جرد مخزني", "إضافة حساب" ->
                        DocumentFormScreen(screen) { screen = "الرئيسية" }
                    else -> HomeScreen(
                        onMenu = { drawerOpen = true },
                        onNavigate = { screen = it }
                    )
                }

                if (drawerOpen) {
                    DrawerOverlay(
                        onClose = { drawerOpen = false },
                        onNavigate = {
                            drawerOpen = false
                            screen = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponsiveScale(content: @Composable (Float) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = (maxWidth.value / 360f).coerceIn(0.90f, 1.35f)
        content(scale)
    }
}

@Composable
private fun AppTopBar(
    title: String,
    onMenu: (() -> Unit)? = null,
    showSearch: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val width = LocalConfiguration.current.screenWidthDp
    val iconSize = if (width < 340) 21.dp else 23.dp

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).background(HesabiBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (onMenu != null) onMenu() else onBack?.invoke() },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                if (onMenu != null) Icons.Default.Menu else Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )
        if (showSearch) {
            IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Search, "بحث", tint = Color.White, modifier = Modifier.size(iconSize))
            }
        }
        IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.Notifications, "التنبيهات", tint = Color.White, modifier = Modifier.size(iconSize))
        }
        if (onMenu != null) {
            IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Share, "مشاركة", tint = Color.White, modifier = Modifier.size(iconSize))
            }
        }
    }
}

@Composable
private fun HomeScreen(onMenu: () -> Unit, onNavigate: (String) -> Unit) {
    ResponsiveScale { scale ->
        var openSection by remember { mutableStateOf("عمليات مخزنية") }
        val sections = listOf("عمليات مخزنية","قيود وحسابات","الأصناف","العملات","التقارير")
        Column(Modifier.fillMaxSize().background(Color.White)) {
            AppTopBar("حسابي", onMenu = onMenu)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 2.dp)
            ) {
                item {
                    Spacer(Modifier.height((10 * scale).dp.coerceAtLeast(8.dp)))
                    Row(Modifier.fillMaxWidth().padding(horizontal = (26 * scale).dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OperationButton("قبض/صرف", Icons.Default.AccountBalanceWallet, scale) { onNavigate("قبض/صرف") }
                        OperationButton("المبيعات", Icons.Default.ShoppingCart, scale) { onNavigate("المبيعات") }
                    }
                    Spacer(Modifier.height((9 * scale).dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = (26 * scale).dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OperationButton("الحسابات", Icons.Default.AccountBalance, scale) { onNavigate("الدليل") }
                        OperationButton("المشتريات", Icons.Default.ShoppingBasket, scale) { onNavigate("المشتريات") }
                    }
                    Spacer(Modifier.height((13 * scale).dp))
                }
                item {
                    SectionBar("عمليات مخزنية", openSection == "عمليات مخزنية", scale) {
                        openSection = if (openSection == "عمليات مخزنية") "" else "عمليات مخزنية"
                    }
                }
                if (openSection == "عمليات مخزنية") {
                    listOf("صرف مخزني","توريد مخزني","تحويل مخزني","تسوية مخزنية","إضافة معدن","جرد مخزني").forEach { name ->
                        item { PlainRow(name, scale) { onNavigate(name) } }
                    }
                }
                item {
                    SectionBar("قيود وحسابات", openSection == "قيود وحسابات", scale) {
                        openSection = if (openSection == "قيود وحسابات") "" else "قيود وحسابات"
                    }
                }
                if (openSection == "قيود وحسابات") {
                    listOf("قيد يومي","قيد افتتاحي","إضافة حساب").forEach { name ->
                        item { PlainRow(name, scale) { onNavigate(name) } }
                    }
                    item { PlainRow("حركة الصندوق", scale) { onNavigate("قبض/صرف") } }
                    item { PlainRow("دليل الحسابات", scale) { onNavigate("الدليل") } }
                    item { PlainRow("العملاء", scale) { onNavigate("العملاء") } }
                    item { PlainRow("الموردون", scale) { onNavigate("الموردون") } }
                }
                item {
                    SectionBar("الأصناف", openSection == "الأصناف", scale) {
                        openSection = if (openSection == "الأصناف") "" else "الأصناف"
                    }
                }
                if (openSection == "الأصناف") {
                    item { PlainRow("الأصناف", scale) { onNavigate("الأصناف") } }
                    item { PlainRow("حركة الأصناف", scale) { onNavigate("حركة الأصناف") } }
                    item { PlainRow("أسعار البيع", scale) { onNavigate("الأصناف") } }
                    item { PlainRow("وحدات الصنف", scale) { onNavigate("الأصناف") } }
                    item { PlainRow("فاتورة عرض بسعر", scale) { onNavigate("عرض سعر") } }
                    item { PlainRow("طلبية شراء", scale) { onNavigate("طلبية شراء") } }
                }
                item {
                    SectionBar("العملات", openSection == "العملات", scale) {
                        openSection = if (openSection == "العملات") "" else "العملات"
                    }
                }
                if (openSection == "العملات") {
                    item { PlainRow("العملات وأسعار الصرف", scale) { onNavigate("العملات") } }
                }
                item {
                    SectionBar("التقارير", openSection == "التقارير", scale) {
                        openSection = if (openSection == "التقارير") "" else "التقارير"
                    }
                }
                if (openSection == "التقارير") {
                    item { PlainRow("حركة الصندوق", scale) { onNavigate("قبض/صرف") } }
                    item { PlainRow("تقارير أخرى", scale) { onNavigate("تقارير أخرى") } }
                }
            }
            BottomWarehouseBar(scale)
        }
    }
}

@Composable
private fun OperationButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width((112 * scale).dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width((82 * scale).dp).height((58 * scale).dp),
            shape = MaterialTheme.shapes.small,
            color = HesabiBlue,
            shadowElevation = 1.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size((24 * scale).dp))
                Text(
                    title,
                    color = Color.White,
                    fontSize = (15f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-5 * scale).dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = HesabiBlueDark
        ) {
            Icon(
                Icons.Default.Add,
                null,
                tint = Color.White,
                modifier = Modifier.padding((3 * scale).dp).size((17 * scale).dp)
            )
        }
    }
}

@Composable
private fun SectionBar(title: String, expanded: Boolean, scale: Float, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height((37 * scale).dp.coerceAtLeast(34.dp))
            .background(HesabiBlue).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null,
            tint = Color.White,
            modifier = Modifier.padding(start = 5.dp).size((23 * scale).dp)
        )
        Text(
            title,
            color = Color.White,
            fontSize = (14.5f * scale).sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f).padding(horizontal = 9.dp)
        )
    }
}

@Composable
private fun PlainRow(title: String, scale: Float, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().height((38 * scale).dp.coerceAtLeast(35.dp))
            .background(Color.White).clickable(onClick = onClick)
            .padding(horizontal = (17 * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = (14f * scale).sp,
            color = HesabiText,
            modifier = Modifier.weight(1f)
        )
    }
    Divider(color = HesabiLine, thickness = 1.dp)
}

@Composable
private fun BottomWarehouseBar(scale: Float = 1f, showAdd: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().height((55 * scale).dp.coerceAtLeast(52.dp))
            .background(HesabiBlueDark),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warehouse,
            null,
            tint = Color.White,
            modifier = Modifier.padding(horizontal = (9 * scale).dp).size((28 * scale).dp)
        )
        Text(
            "المخزن الرئيسي",
            color = Color.White,
            fontSize = (14f * scale).sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.SwapHoriz,
            null,
            tint = Color.White,
            modifier = Modifier.padding(end = (12 * scale).dp).size((28 * scale).dp)
        )
        if (showAdd) {
            Icon(
                Icons.Default.AddCircle,
                null,
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp).size(29.dp)
            )
        }
    }
}

@Composable
private fun DrawerOverlay(onClose: () -> Unit, onNavigate: (String) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier.weight(1f).fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onClose)
        )
        Column(
            Modifier.width(310.dp).fillMaxHeight().background(Color.White)
        ) {
            Row(
                Modifier.fillMaxWidth().height(62.dp).background(HesabiBlue),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(horizontal = 17.dp)) {
                    Text("حسابي", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("(0) غير المنفذ", color = Color.White.copy(alpha = .9f), fontSize = 12.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            DrawerRow("حفظ نسخة", Icons.Default.Save) { onNavigate("الرئيسية") }
            DrawerRow("استعادة قاعدة", Icons.Default.Restore) { onNavigate("الرئيسية") }
            DrawerRow("جوجل درايف", Icons.Default.CloudUpload) { onNavigate("الرئيسية") }
            DrawerRow("دليل الحسابات", Icons.Default.AccountTree) { onNavigate("الدليل") }
            DrawerRow("إعدادات", Icons.Default.Settings) { onNavigate("الإعدادات") }
            DrawerRow("للتواصل والدعم", Icons.Default.Phone) { onNavigate("الرئيسية") }
            DrawerRow("حول البرنامج", Icons.Default.Info) { onNavigate("الرئيسية") }
            DrawerRow("خروج", Icons.Default.ExitToApp) { onClose() }
        }
    }
}

@Composable
private fun DrawerRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(46.dp).clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = HesabiBlue, modifier = Modifier.size(22.dp))
        Text(title, fontSize = 14.sp, color = HesabiText, modifier = Modifier.weight(1f).padding(horizontal = 9.dp))
    }
    Divider(color = HesabiLine)
}

@Composable
private fun ProductsScreen(onBack: () -> Unit) {
    ResponsiveScale { scale ->
        val products = listOf(
            Triple("ركب أمريكي سن نحاس 1/2 هـ", "سباكة", "0"),
            Triple("مسامير خشب صيني 8×8", "مسامير", "0"),
            Triple("مسامير خشب صيني 8×1", "مسامير", "0"),
            Triple("ركب أمريكي سن نحاس 3/4 هـ", "سباكة", "0"),
            Triple("سلك أمريكي كيس 1/2 هـ", "سلك", "0"),
            Triple("ركب أمريكي سن نحاس 1/2 * 3/4", "سباكة", "0")
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar("الأصناف", showSearch = true, onBack = onBack)
            ProductHeader(scale)
            LazyColumn(Modifier.weight(1f)) {
                items(products) { p ->
                    Row(
                        Modifier.fillMaxWidth().height((57 * scale).dp.coerceAtLeast(52.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.third, fontSize = (14 * scale).sp, modifier = Modifier.width(62.dp))
                        Text(p.second, fontSize = (14 * scale).sp, modifier = Modifier.width(86.dp))
                        Text(p.first, fontSize = (14 * scale).sp, modifier = Modifier.weight(1f).padding(horizontal = 7.dp))
                    }
                    Divider(color = HesabiLine)
                }
            }
            BottomWarehouseBar(scale, showAdd = true)
        }
    }
}

@Composable
private fun ProductHeader(scale: Float) {
    Row(
        Modifier.fillMaxWidth().height((42 * scale).dp).background(HesabiHeader),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("الرصيد", fontSize = (13.5f * scale).sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(62.dp))
        Text("المجموعة", fontSize = (13.5f * scale).sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(86.dp))
        Text("اسم الصنف", fontSize = (13.5f * scale).sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.Search, null, tint = HesabiBlue, modifier = Modifier.padding(horizontal = 7.dp).size(21.dp))
        Icon(Icons.Default.MoreVert, null, tint = HesabiBlue, modifier = Modifier.padding(end = 5.dp).size(21.dp))
    }
}

@Composable
private fun StockMovementScreen(onBack: () -> Unit) {
    ResponsiveScale { scale ->
        val rows = listOf(
            arrayOf("294", "188", "106", "ركب أمريكي سن نحاس 1/2 هـ"),
            arrayOf("160", "112", "48", "مسامير خشب صيني 8×8"),
            arrayOf("33.575", "13.3", "20.275", "مسامير خشب صيني 8×1"),
            arrayOf("72", "34", "38", "ركب أمريكي سن نحاس 3/4 هـ"),
            arrayOf("28", "9", "19", "ركب أمريكي سن نحاس 1/2 * 3/4"),
            arrayOf("100", "70", "30", "سلك أمريكي كيس 1/2 هـ"),
            arrayOf("161", "12", "149", "سلك أمريكي كيس 3/4 هـ"),
            arrayOf("2.928", "2.126", "0.802", "مواسير 1.5 * 1.8 هايل بلس")
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar("حركة الأصناف", showSearch = true, onBack = onBack)
            Row(
                Modifier.fillMaxWidth().height((43 * scale).dp).background(HesabiBlue),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الصنف", Modifier.weight(1f).padding(horizontal = 8.dp), color = Color.White, fontSize = (13.5 * scale).sp)
                Text("المنصرف", Modifier.width(62.dp), color = Color.White, fontSize = (13.5 * scale).sp)
                Text("الوارد", Modifier.width(62.dp), color = Color.White, fontSize = (13.5 * scale).sp)
                Text("المتاح", Modifier.width(62.dp), color = Color.White, fontSize = (13.5 * scale).sp)
            }
            LazyColumn(Modifier.weight(1f)) {
                items(rows) { r ->
                    Row(Modifier.fillMaxWidth().height((56 * scale).dp.coerceAtLeast(52.dp)), verticalAlignment = Alignment.CenterVertically) {
                        Text(r[3].replace("\n", "\n"), Modifier.weight(1f).padding(horizontal = 8.dp), fontSize = (13.5 * scale).sp)
                        Text(r[1], Modifier.width(62.dp), fontSize = (13.5 * scale).sp)
                        Text(r[0], Modifier.width(62.dp), fontSize = (13.5 * scale).sp)
                        Text(r[2], Modifier.width(62.dp), fontSize = (13.5 * scale).sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = HesabiLine)
                }
            }
            BottomWarehouseBar(scale, showAdd = true)
        }
    }
}

@Composable
private fun TransactionMenuScreen(title: String, onBack: () -> Unit) {
    val rows = when (title) {
        "المبيعات" -> listOf("فاتورة مبيعات","مرتجع مبيعات","عرض سعر","طلبات العملاء","كشف حساب عميل","حركة المبيعات")
        "المشتريات" -> listOf("فاتورة مشتريات","مرتجع مشتريات","طلبات الشراء","طلبية شراء","كشف حساب مورد","حركة المشتريات")
        else -> listOf("قبض","صرف","تحويل بين الصناديق والبنوك","حركة الصندوق","حركة البنك")
    }
    Column(Modifier.fillMaxSize()) {
        AppTopBar(title, onBack = onBack, showSearch = true)
        LazyColumn(Modifier.weight(1f)) {
            items(rows) { row ->
                PlainRow(row, 1f) {
                    when (row) {
                        "فاتورة مبيعات","مرتجع مبيعات","عرض سعر","فاتورة مشتريات","مرتجع مشتريات","طلبية شراء","قبض","صرف" -> {
                            // Navigation is handled by the parent route in the main screen.
                        }
                    }
                }
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun SimpleListScreen(title: String, rows: List<String>, onBack: () -> Unit) {
    ResponsiveScale { scale ->
        Column(Modifier.fillMaxSize().background(Color.White)) {
            AppTopBar(title, onBack = onBack, showSearch = true)
            LazyColumn(Modifier.weight(1f)) {
                items(rows) { row ->
                    PlainRow(row, scale)
                }
            }
            BottomWarehouseBar(scale, showAdd = true)
        }
    }
}

@Composable
private fun StockOperationsScreen(onBack: () -> Unit) {
    val rows = listOf("صرف مخزني","توريد مخزني","تحويل مخزني","تسوية مخزنية","إضافة معدن","جرد مخزني","حركة الأصناف","الأصناف الناقصة","الأصناف ذات الرصيد الصفري")
    Column(Modifier.fillMaxSize()) {
        AppTopBar("عمليات مخزنية", onBack = onBack, showSearch = true)
        LazyColumn(Modifier.weight(1f)) {
            items(rows) { row ->
                PlainRow(row, 1f)
            }
        }
        BottomWarehouseBar(showAdd = true)
    }
}

@Composable
private fun AccountingOperationsScreen(onBack: () -> Unit) {
    val rows = listOf("قيد يومي","قيد افتتاحي","إضافة حساب","حركة الصندوق","حركة الحسابات","دليل الحسابات","العملاء","الموردون","سند قبض","سند صرف")
    Column(Modifier.fillMaxSize()) {
        AppTopBar("قيود وحسابات", onBack = onBack, showSearch = true)
        LazyColumn(Modifier.weight(1f)) {
            items(rows) { row ->
                PlainRow(row, 1f)
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun CurrenciesScreen(onBack: () -> Unit) {
    ResponsiveScale { scale ->
        val rows = listOf(
            Triple("ريال يمني","YER","العملة الأساسية"),
            Triple("ريال سعودي","SAR","سعر الصرف"),
            Triple("دولار أمريكي","USD","سعر الصرف")
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar("العملات وأسعار الصرف", onBack = onBack, showSearch = true)
            Row(Modifier.fillMaxWidth().height((42*scale).dp).background(HesabiBlue), verticalAlignment = Alignment.CenterVertically) {
                Text("العملة", Modifier.weight(1f).padding(horizontal=12.dp), color=Color.White, fontSize=(14*scale).sp, fontWeight=FontWeight.Bold)
                Text("الرمز", Modifier.width(70.dp), color=Color.White, fontSize=(14*scale).sp, fontWeight=FontWeight.Bold)
                Text("الحالة", Modifier.width(110.dp), color=Color.White, fontSize=(14*scale).sp, fontWeight=FontWeight.Bold)
            }
            LazyColumn(Modifier.weight(1f)) {
                items(rows) { r ->
                    Row(Modifier.fillMaxWidth().height((54*scale).dp), verticalAlignment=Alignment.CenterVertically) {
                        Text(r.first, Modifier.weight(1f).padding(horizontal=12.dp), fontSize=(14*scale).sp)
                        Text(r.second, Modifier.width(70.dp), fontSize=(14*scale).sp)
                        Text(r.third, Modifier.width(110.dp), fontSize=(13*scale).sp)
                    }
                    Divider(color=HesabiLine)
                }
            }
            BottomWarehouseBar(scale, showAdd=true)
        }
    }
}

@Composable
private fun DocumentFormScreen(title: String, onBack: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var party by remember { mutableStateOf("") }
    var item by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val isCash = title == "قبض" || title == "صرف"
    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppTopBar(title, onBack = onBack)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp)
        ) {
            item {
                FormField(if (title.contains("فاتورة") || title.contains("مرتجع") || title=="عرض سعر" || title=="طلبية شراء") "رقم المستند" else "المرجع", number) { number = it }
                FormField(if (isCash) "الحساب" else if (title.contains("شراء") || title.contains("مرتجع مشتريات")) "المورد" else "العميل", party) { party = it }
                if (!isCash && title != "قيد يومي" && title != "قيد افتتاحي" && title != "إضافة حساب") {
                    FormField("الصنف", item) { item = it }
                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) { FormField("الكمية", quantity) { quantity = it } }
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) { FormField("السعر", price) { price = it } }
                    }
                }
                if (title == "قيد يومي" || title == "قيد افتتاحي") {
                    FormField("البيان", notes) { notes = it }
                    FormField("الحساب المدين", party) { party = it }
                    FormField("الحساب الدائن", item) { item = it }
                    FormField("المبلغ", price) { price = it }
                } else if (title == "إضافة حساب") {
                    FormField("اسم الحساب", party) { party = it }
                    FormField("نوع الحساب", item) { item = it }
                } else {
                    FormField("ملاحظات", notes) { notes = it }
                }
                Spacer(Modifier.height(8.dp))
                SummaryLine("الإجمالي", if (price.isBlank()) "0.00" else price)
                SummaryLine("المدفوع", "0.00")
                SummaryLine("المتبقي", if (price.isBlank()) "0.00" else price)
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HesabiBlue)
        ) {
            Text("حفظ وترحيل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        singleLine = true
    )
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = HesabiText)
        Text(value, fontWeight = FontWeight.Bold, color = HesabiBlue)
    }
}

@Composable
private fun AccountsScreen(onBack: () -> Unit) {
    ResponsiveScale { scale ->
        val accounts = listOf(
            Triple("1752", "أصول", "رئيسي"),
            Triple("96", "التزامات وحقوق الملكية", "رئيسي"),
            Triple("36", "مصروفات", "رئيسي"),
            Triple("9", "إيرادات", "رئيسي")
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar("الدليل المحاسبي", showSearch = true, onBack = onBack)
            Row(Modifier.fillMaxWidth().height((42 * scale).dp).background(HesabiBlue), verticalAlignment = Alignment.CenterVertically) {
                Text("الرصيد", Modifier.width(72.dp), color = Color.White, fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold)
                Text("نوع الحساب", Modifier.width(125.dp), color = Color.White, fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold)
                Text("اسم الحساب", Modifier.weight(1f), color = Color.White, fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold)
            }
            LazyColumn(Modifier.weight(1f)) {
                items(accounts) { a ->
                    Row(Modifier.fillMaxWidth().height((58 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.first, Modifier.width(72.dp), fontSize = (14 * scale).sp)
                        Text(a.second, Modifier.width(125.dp), fontSize = (14 * scale).sp)
                        Text(a.third, Modifier.weight(1f), fontSize = (14 * scale).sp)
                    }
                    Divider(color = HesabiLine)
                }
            }
            BottomWarehouseBar(scale, showAdd = true)
        }
    }
}

@Composable
private fun ReportsScreen(onBack: () -> Unit) {
    val reports = listOf(
        "المخزون المتبقي", "أرصدة الحسابات", "أرباح الأصناف", "العمليات اليومية",
        "القيود اليومية", "حركة الصندوق", "حركة الحسابات", "إجمالي الخصومات",
        "فوارق أسعار العملات", "رأس المال العامل", "الربح على مستوى العميل",
        "المبيعات حسب الصنف", "المشتريات حسب الصنف", "تفاصيل حركة الأصناف"
    )
    Column(Modifier.fillMaxSize()) {
        AppTopBar("تقارير أخرى", onBack = onBack)
        LazyColumn(Modifier.weight(1f)) {
            items(reports) { r ->
                Row(
                    Modifier.fillMaxWidth().height(46.dp).clickable {},
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(r, Modifier.weight(1f).padding(horizontal = 18.dp), fontSize = 14.sp)
                    Icon(Icons.Default.Assessment, null, tint = HesabiBlue, modifier = Modifier.padding(end = 12.dp).size(21.dp))
                }
                Divider(color = HesabiLine)
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val settings = listOf(
        "البيانات الشخصية", "خيارات الطباعة", "خيارات الأمان", "المستخدمين والصلاحيات",
        "التنبيهات", "مجموعة الصنف", "وحدات القياس", "خيارات حفظ البيانات",
        "الطابعة الحرارية", "الضريبة", "طابعة باركود الأصناف", "خيارات الإشعارات", "خيارات أخرى"
    )
    Column(Modifier.fillMaxSize()) {
        AppTopBar("إعدادات", onBack = onBack)
        LazyColumn(Modifier.weight(1f)) {
            items(settings) { s ->
                Row(
                    Modifier.fillMaxWidth().height(46.dp).clickable {},
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = HesabiBlue, modifier = Modifier.padding(horizontal = 11.dp).size(19.dp))
                    Text(s, fontSize = 14.sp)
                }
                Divider(color = HesabiLine)
            }
        }
    }
}

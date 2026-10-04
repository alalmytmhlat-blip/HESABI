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
private val HesabiLine = Color(0xFFE3E3E3)

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
                onSurface = Color(0xFF222222)
            )
        ) {
            Box(Modifier.fillMaxSize().background(Color.White)) {
                when (screen) {
                    "الأصناف" -> SimpleListScreen("الأصناف", listOf("ركب أمريكي سن نحاس 1/2 هـ", "مسمار خشب صيني 8×8", "مسمار خشب صيني 8×1", "ركب أمريكي سن نحاس 3/4 هـ", "سلك أمريكي كيس 1/2 هـ"))
                    "حركة الأصناف" -> StockMovementScreen()
                    "تقارير أخرى" -> ReportsScreen()
                    "الإعدادات" -> SettingsScreen()
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
private fun AppTopBar(
    title: String = "حسابي",
    onMenu: (() -> Unit)? = null,
    showSearch: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).background(HesabiBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onMenu != null) {
            IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Menu, "القائمة", tint = Color.White)
            }
        } else {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {}, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
            }
        }

        Text(
            title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (showSearch) {
            IconButton(onClick = {}, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.Search, "بحث", tint = Color.White)
            }
        }
        IconButton(onClick = {}, modifier = Modifier.size(42.dp)) {
            Icon(Icons.Default.Notifications, "التنبيهات", tint = Color.White)
        }
        IconButton(onClick = {}, modifier = Modifier.size(42.dp)) {
            Icon(Icons.Default.Share, "مشاركة", tint = Color.White)
        }
    }
}

@Composable
private fun HomeScreen(
    onMenu: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var openSection by remember { mutableStateOf("العمليات المخزنية") }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        AppTopBar("حسابي", onMenu)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 34.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OperationButton("المبيعات", Icons.Default.ShoppingCart) { onNavigate("المبيعات") }
                    OperationButton("قبض/صرف", Icons.Default.AccountBalanceWallet) { onNavigate("قبض/صرف") }
                }
            }
            item {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 34.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OperationButton("المشتريات", Icons.Default.ShoppingBasket) { onNavigate("المشتريات") }
                    OperationButton("الحسابات", Icons.Default.AccountBalance) { onNavigate("الحسابات") }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                SectionBar("عمليات مخزنية", openSection == "العمليات المخزنية") {
                    openSection = if (openSection == "العمليات المخزنية") "" else "العمليات المخزنية"
                }
            }
            if (openSection == "العمليات المخزنية") {
                item { PlainRow("إضافة مخزن") }
                item { PlainRow("جرد مخزني") }
                item { PlainRow("تحويل مخزني") }
            }

            item {
                SectionBar("قيود وحسابات", openSection == "قيود وحسابات") {
                    openSection = if (openSection == "قيود وحسابات") "" else "قيود وحسابات"
                }
            }
            if (openSection == "قيود وحسابات") {
                item { PlainRow("قيد يومي") }
                item { PlainRow("قيود تلقائية") }
                item { PlainRow("حركة الصندوق") }
                item { PlainRow("دليل الحسابات") }
            }

            item {
                SectionBar("أصناف", openSection == "أصناف") {
                    openSection = if (openSection == "أصناف") "" else "أصناف"
                }
            }
            if (openSection == "أصناف") {
                item { PlainRow("الأصناف", onClick = { onNavigate("الأصناف") }) }
                item { PlainRow("حركة الأصناف", onClick = { onNavigate("حركة الأصناف") }) }
            }

            item {
                SectionBar("العملات", openSection == "العملات") {
                    openSection = if (openSection == "العملات") "" else "العملات"
                }
            }

            item {
                SectionBar("التقارير", openSection == "التقارير") {
                    openSection = if (openSection == "التقارير") "" else "التقارير"
                }
            }
            if (openSection == "التقارير") {
                item { PlainRow("تقارير أخرى", onClick = { onNavigate("تقارير أخرى") }) }
            }
        }

        BottomWarehouseBar()
    }
}

@Composable
private fun OperationButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(115.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = HesabiBlue,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.width(82.dp).height(52.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 10.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = HesabiBlueDark
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.padding(3.dp).size(16.dp))
            }
        }
    }
}

@Composable
private fun SectionBar(title: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(34.dp).background(HesabiBlue).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null, tint = Color.White, modifier = Modifier.padding(start = 6.dp).size(21.dp)
        )
        Text(title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
    }
}

@Composable
private fun PlainRow(title: String, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().height(30.dp).background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
    }
    Divider(color = HesabiLine, thickness = 1.dp)
}

@Composable
private fun BottomWarehouseBar() {
    Row(
        Modifier.fillMaxWidth().height(55.dp).background(HesabiBlueDark),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.small,
            color = Color.Transparent
        ) {
            Icon(Icons.Default.Warehouse, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Text("المخزن الرئيسي", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.SwapHoriz, null, tint = Color.White, modifier = Modifier.padding(end = 12.dp).size(28.dp))
    }
}

@Composable
private fun DrawerOverlay(onClose: () -> Unit, onNavigate: (String) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier.weight(1f).fillMaxHeight().background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onClose)
        )
        Column(
            Modifier.width(305.dp).fillMaxHeight().background(Color.White)
        ) {
            Row(
                Modifier.fillMaxWidth().height(60.dp).background(HesabiBlue),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("حسابي", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(horizontal = 18.dp))
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }
            DrawerRow("صفحة رئيسية", Icons.Default.Home) { onNavigate("الرئيسية") }
            DrawerRow("استرجاع قاعدة", Icons.Default.Restore) { onNavigate("الرئيسية") }
            DrawerRow("جوجل درايف", Icons.Default.CloudUpload) { onNavigate("الرئيسية") }
            DrawerRow("دليل الحسابات", Icons.Default.AccountTree) { onNavigate("الرئيسية") }
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
        Modifier.fillMaxWidth().height(43.dp).clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = HesabiBlue, modifier = Modifier.size(22.dp))
        Text(title, fontSize = 13.sp, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
    }
    Divider(color = HesabiLine)
}

@Composable
private fun SimpleListScreen(title: String, data: List<String>) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(title, showSearch = true)
        LazyColumn(Modifier.weight(1f)) {
            items(data) { name ->
                Row(
                    Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("0", fontSize = 13.sp, modifier = Modifier.width(50.dp))
                    Text("0", fontSize = 13.sp, modifier = Modifier.width(50.dp))
                }
                Divider(color = HesabiLine)
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun StockMovementScreen() {
    val rows = listOf(
        Triple("ركب أمريكي سن نحاس 1/2 هـ", "294", "188"),
        Triple("مسمار خشب صيني 8×8", "160", "112"),
        Triple("مسمار خشب صيني 8×1", "33.575", "13.3"),
        Triple("ركب أمريكي سن نحاس 3/4 هـ", "72", "34"),
        Triple("سلك أمريكي كيس 1/2 هـ", "100", "70")
    )
    Column(Modifier.fillMaxSize()) {
        AppTopBar("حركة الأصناف", showSearch = true)
        Row(Modifier.fillMaxWidth().height(38.dp).background(Color(0xFFF7F7F7)), verticalAlignment = Alignment.CenterVertically) {
            Text("الصنف", Modifier.weight(1f).padding(horizontal = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("الوارد", Modifier.width(65.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("المنصرف", Modifier.width(65.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("المتاح", Modifier.width(65.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LazyColumn(Modifier.weight(1f)) {
            items(rows) { r ->
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(r.first, Modifier.weight(1f).padding(horizontal = 12.dp), fontSize = 12.sp)
                    Text(r.second, Modifier.width(65.dp), fontSize = 12.sp)
                    Text(r.third, Modifier.width(65.dp), fontSize = 12.sp)
                    Text("106", Modifier.width(65.dp), fontSize = 12.sp)
                }
                Divider(color = HesabiLine)
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun ReportsScreen() {
    val reports = listOf("الموازين الافتتاحية", "أرصدة الحسابات", "أرباح الأصناف", "العمليات اليومية", "القيود اليومية", "حركة الصندوق", "حركة الحسابات", "إجمالي الخصومات", "فوارق أسعار العملات", "رأس المال العامل", "الربح على مستوى العميل", "المبيعات حسب الصنف", "المشتريات حسب الصنف", "تفاصيل حركة الأصناف")
    Column(Modifier.fillMaxSize()) {
        AppTopBar("تقارير أخرى")
        LazyColumn(Modifier.weight(1f)) {
            items(reports) { r ->
                Row(Modifier.fillMaxWidth().height(43.dp).clickable {}, verticalAlignment = Alignment.CenterVertically) {
                    Text(r, Modifier.weight(1f).padding(horizontal = 18.dp), fontSize = 12.sp)
                    Icon(Icons.Default.Assessment, null, tint = HesabiBlue, modifier = Modifier.padding(end = 12.dp).size(20.dp))
                }
                Divider(color = HesabiLine)
            }
        }
        BottomWarehouseBar()
    }
}

@Composable
private fun SettingsScreen() {
    val settings = listOf("البيانات الشخصية", "خيارات الطباعة", "خيارات الأمان", "المستخدمين والصلاحيات", "التنبيهات", "مجموعة الصنف", "وحدات القياس", "خيارات حفظ البيانات", "الطابعة الحرارية", "الضريبة", "طابعة باركود الأصناف", "خيارات الإشعارات", "خيارات أخرى")
    Column(Modifier.fillMaxSize()) {
        AppTopBar("إعدادات")
        LazyColumn(Modifier.weight(1f)) {
            items(settings) { s ->
                Row(Modifier.fillMaxWidth().height(40.dp).clickable {}, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = HesabiBlue, modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))
                    Text(s, fontSize = 12.sp)
                }
                Divider(color = HesabiLine)
            }
        }
        BottomWarehouseBar()
    }
}

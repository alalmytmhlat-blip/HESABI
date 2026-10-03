package com.hesabi.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hesabi.app.data.AppDatabase
import com.hesabi.app.data.InitialData
import kotlinx.coroutines.launch

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{HesabiApp()}}}

@Composable
fun HesabiApp(){
 val context=LocalContext.current
 val db=remember{AppDatabase.get(context)}
 val scope=rememberCoroutineScope()
 var tab by remember{mutableIntStateOf(0)}
 LaunchedEffect(Unit){scope.launch{InitialData(db).seed()}}
 val titles=listOf("الرئيسية","المبيعات","المشتريات","العملاء","المزيد")
 val icons=listOf(Icons.Default.Home,Icons.Default.ShoppingCart,Icons.Default.ShoppingCart,Icons.Default.People,Icons.Default.Menu)
 CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
  MaterialTheme{
   Scaffold(
    topBar={TopAppBar(title={Text("حسابي",fontWeight=FontWeight.Bold)},actions={IconButton({}){Icon(Icons.Default.Notifications,null)}})},
    bottomBar={NavigationBar{titles.forEachIndexed{i,t->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],null)},label={Text(t)})}}},
    content={p->when(tab){
     0->Dashboard(p,db)
     1->ModulePage(p,"المبيعات",listOf("فاتورة مبيعات","مرتجع مبيعات","فواتير المبيعات","كشف حساب العملاء","قبض"))
     2->ModulePage(p,"المشتريات",listOf("فاتورة مشتريات","مرتجع مشتريات","فواتير المشتريات","كشف حساب الموردين","صرف"))
     3->ContactsPage(p,db)
     else->MorePage(p)
    }}
   )
  }
 }
}

@Composable
fun Dashboard(p:PaddingValues,db:AppDatabase){
 val products by db.products().all().collectAsState(initial=emptyList())
 val contacts by db.contacts().all().collectAsState(initial=emptyList())
 val low by db.products().low().collectAsState(initial=emptyList())
 LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Text("لوحة التحكم",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("نظرة سريعة على نشاط المنشأة",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()){StatCard("العملاء",contacts.size.toString(),Modifier.weight(1f));StatCard("الأصناف",products.size.toString(),Modifier.weight(1f));StatCard("منخفض المخزون",low.size.toString(),Modifier.weight(1f))}}
  item{Text("الوصول السريع",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
  items(listOf("فاتورة مبيعات","فاتورة مشتريات","قبض","صرف","كشف حساب","الأرباح والخسائر","الميزانية العمومية")){QuickCard(it)}
 }
}
@Composable fun StatCard(a:String,b:String,modifier:Modifier){Card(modifier){Column(Modifier.padding(14.dp)){Text(a,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(b,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}}}
@Composable fun QuickCard(t:String){Card{ListItem(headlineContent={Text(t,fontWeight=FontWeight.SemiBold)},leadingContent={Icon(Icons.Default.ArrowBack,null)})}}
@Composable fun ModulePage(p:PaddingValues,title:String,items:List<String>){LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(items){QuickCard(it)}}}
@Composable fun ContactsPage(p:PaddingValues,db:AppDatabase){val data by db.contacts().all().collectAsState(initial=emptyList());LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("العملاء والموردون",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(data){ListItem(headlineContent={Text(it.name)},supportingContent={Text(it.kind+" • الرصيد: "+it.balance)},trailingContent={IconButton({}){Icon(Icons.Default.MoreVert,null)}})}}}
@Composable fun MorePage(p:PaddingValues){LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("المزيد",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(listOf("المخزون والمنتجات","المستودعات","الصندوق والبنوك","المصروفات والإيرادات","الحسابات والقيود اليومية","التقارير","المواعيد والاستحقاقات","النسخ الاحتياطي والاستعادة","الطباعة 58/80mm","المستخدمون والصلاحيات","الإعدادات","المحاسب الذكي")){QuickCard(it)}}}

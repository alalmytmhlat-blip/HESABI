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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hesabi.app.data.AppDatabase

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{HesabiApp()}}}
@Composable fun HesabiApp(){
 val db=remember{AppDatabase.get(LocalContext.current)};var tab by remember{mutableIntStateOf(0)}
 val titles=listOf("الرئيسية","المبيعات","المشتريات","العملاء","المخزون");val icons=listOf(Icons.Default.Home,Icons.Default.ShoppingCart,Icons.Default.ShoppingBag,Icons.Default.People,Icons.Default.Inventory)
 Scaffold(topBar={TopAppBar(title={Text("حسابي",fontWeight=FontWeight.Bold)},actions={IconButton({}){Icon(Icons.Default.Notifications,null)}})},bottomBar={NavigationBar{titles.forEachIndexed{i,t->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],null)},label={Text(t)})}}},floatingActionButton={if(tab>0)FloatingActionButton(onClick={}){Icon(Icons.Default.Add,null)}},content={p->when(tab){0->Dashboard(p,db);1->ModulePage(p,"المبيعات",listOf("فاتورة مبيعات","مرتجع مبيعات","فواتير المبيعات","المدفوعات"));2->ModulePage(p,"المشتريات",listOf("فاتورة مشتريات","مرتجع مشتريات","فواتير المشتريات","المدفوعات للموردين"));3->ContactsPage(p,db);else->ProductsPage(p,db)}})
}
@Composable fun Dashboard(p:PaddingValues,db:AppDatabase){val products by db.products().all().collectAsState(initial=emptyList());val contacts by db.contacts().all().collectAsState(initial=emptyList());Column(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("لوحة التحكم",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("إدارة تجارتك وحساباتك من مكان واحد",color=MaterialTheme.colorScheme.onSurfaceVariant);Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()){StatCard("العملاء",contacts.size.toString());StatCard("الأصناف",products.size.toString())};Text("الوصول السريع",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);listOf("فاتورة مبيعات","فاتورة مشتريات","قبض","صرف","كشف حساب","الأرباح والخسائر").forEach{QuickCard(it)}}}
@Composable fun StatCard(a:String,b:String){Card(Modifier.weight(1f)){Column(Modifier.padding(16.dp)){Text(a);Text(b,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}}}
@Composable fun QuickCard(t:String){Card{ListItem(headlineContent={Text(t,fontWeight=FontWeight.SemiBold)},leadingContent={Icon(Icons.Default.ArrowForward,null)})}}
@Composable fun ModulePage(p:PaddingValues,title:String,items:List<String>){LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(items){QuickCard(it)}}}
@Composable fun ContactsPage(p:PaddingValues,db:AppDatabase){val data by db.contacts().all().collectAsState(initial=emptyList());LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("العملاء والموردون",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(data){ListItem(headlineContent={Text(it.name)},supportingContent={Text(it.kind+" • الرصيد: "+it.balance)})}}}
@Composable fun ProductsPage(p:PaddingValues,db:AppDatabase){val data by db.products().all().collectAsState(initial=emptyList());LazyColumn(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("المخزون",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(data){ListItem(headlineContent={Text(it.name)},supportingContent={Text("الكمية: "+it.quantity+" "+it.unit+" • بيع: "+it.salePrice)})}}}
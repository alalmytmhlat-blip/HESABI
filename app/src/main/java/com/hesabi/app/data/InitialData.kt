package com.hesabi.app.data
import androidx.room.withTransaction
class InitialData(private val db:AppDatabase){
 suspend fun seed(){db.withTransaction{
  val defaults=listOf("الصندوق","البنك","العملاء","الموردون","المبيعات","المشتريات","المصروفات","الإيرادات","رأس المال","المخزون")
  defaults.forEachIndexed{index,name->if(db.accounts().byName(name)==null)db.accounts().insert(Account(name=name,type=if(index<4)"ASSET" else "PL"))}
  if(db.settings().get("base_currency")==null){db.currencies().insert(Currency("YER","ريال يمني","ر.ي",0,true));db.settings().put(AppSetting("base_currency","YER"))}
 }}
}
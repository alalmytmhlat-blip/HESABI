package com.hesabi.app.data
class SettingsRepository(private val db:AppDatabase){
 suspend fun get(key:String,default:String=""):String=db.settings().get(key)?:default
 suspend fun set(key:String,value:String){db.settings().put(AppSetting(key,value))}
 suspend fun companyName()=get("company_name","منشأتي")
 suspend fun companyPhone()=get("company_phone","")
 suspend fun companyAddress()=get("company_address","")
}
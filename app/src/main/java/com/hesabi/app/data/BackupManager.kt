package com.hesabi.app.data
import android.content.Context
import org.json.JSONObject
import java.io.File
class BackupManager(private val context:Context){
 suspend fun createBackup():File{
  val db=context.getDatabasePath("hesabi.db")
  require(db.exists()){"قاعدة البيانات غير موجودة"}
  val target=File(context.cacheDir,"hesabi-backup-${System.currentTimeMillis()}.db")
  db.copyTo(target,true)
  return target
 }
 suspend fun metadata():String{
  val s=SettingsRepository(AppDatabase.get(context))
  return JSONObject().put("app","HESABI").put("company",s.companyName()).put("createdAt",System.currentTimeMillis()).toString()
 }
}
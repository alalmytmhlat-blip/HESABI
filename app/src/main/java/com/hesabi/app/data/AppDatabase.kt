package com.hesabi.app.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="accounts") data class Account(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val type:String,val balance:Double=0.0)
@Entity(tableName="contacts") data class Contact(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val phone:String="",val kind:String="customer",val balance:Double=0.0)
@Entity(tableName="products") data class Product(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val barcode:String="",val unit:String="حبة",val salePrice:Double=0.0,val purchasePrice:Double=0.0,val quantity:Double=0.0,val minQuantity:Double=0.0)
@Dao interface AccountDao{@Query("SELECT * FROM accounts ORDER BY name") fun all():Flow<List<Account>>;@Insert suspend fun insert(x:Account);@Query("SELECT * FROM accounts WHERE name=:name LIMIT 1") suspend fun byName(name:String):Account?}
@Dao interface ContactDao{@Query("SELECT * FROM contacts ORDER BY name") fun all():Flow<List<Contact>>;@Insert suspend fun insert(x:Contact):Long;@Update suspend fun update(x:Contact)}
@Dao interface ProductDao{@Query("SELECT * FROM products ORDER BY name") fun all():Flow<List<Product>>;@Query("SELECT * FROM products WHERE quantity<=minQuantity ORDER BY name") fun low():Flow<List<Product>>;@Insert suspend fun insert(x:Product):Long;@Update suspend fun update(x:Product);@Query("SELECT * FROM products WHERE id=:id") suspend fun byId(id:Long):Product?}
@Database(entities=[Account::class,Contact::class,Product::class,Warehouse::class,Currency::class,Invoice::class,InvoiceLine::class,JournalEntry::class,JournalLine::class,CashTransaction::class,Appointment::class,AppSetting::class],version=2,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){
 abstract fun accounts():AccountDao;abstract fun contacts():ContactDao;abstract fun products():ProductDao;abstract fun warehouses():WarehouseDao;abstract fun currencies():CurrencyDao;abstract fun invoices():InvoiceDao;abstract fun invoiceLines():InvoiceLineDao;abstract fun journals():JournalDao;abstract fun cash():CashDao;abstract fun appointments():AppointmentDao;abstract fun settings():SettingDao
 companion object{@Volatile private var INSTANCE:AppDatabase?=null
 fun get(c:android.content.Context):AppDatabase=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"hesabi.db").fallbackToDestructiveMigration().build().also{INSTANCE=it}}}
}
package com.hesabi.app.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="accounts") data class Account(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val type:String,val balance:Double=0.0)
@Entity(tableName="contacts") data class Contact(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val phone:String="",val kind:String="customer",val balance:Double=0.0)
@Entity(tableName="products") data class Product(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val barcode:String="",val unit:String="حبة",val salePrice:Double=0.0,val purchasePrice:Double=0.0,val quantity:Double=0.0,val minQuantity:Double=0.0)
@Dao interface AccountDao{@Query("SELECT * FROM accounts ORDER BY name") fun all():Flow<List<Account>>;@Insert suspend fun insert(x:Account)}
@Dao interface ContactDao{@Query("SELECT * FROM contacts ORDER BY name") fun all():Flow<List<Contact>>;@Insert suspend fun insert(x:Contact)}
@Dao interface ProductDao{@Query("SELECT * FROM products ORDER BY name") fun all():Flow<List<Product>>;@Query("SELECT * FROM products WHERE quantity<=minQuantity ORDER BY name") fun low():Flow<List<Product>>;@Insert suspend fun insert(x:Product)}
@Database(entities=[Account::class,Contact::class,Product::class],version=1,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){abstract fun accounts():AccountDao;abstract fun contacts():ContactDao;abstract fun products():ProductDao
companion object{@Volatile private var INSTANCE:AppDatabase?=null
fun get(c:android.content.Context):AppDatabase=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"hesabi.db").build().also{INSTANCE=it}}}}
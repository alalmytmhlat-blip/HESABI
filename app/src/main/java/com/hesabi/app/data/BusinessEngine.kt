package com.hesabi.app.data
import androidx.room.withTransaction
data class InvoiceDraft(val type:String,val number:String,val contactId:Long?,val warehouseId:Long?,val currencyCode:String,val exchangeRate:Double,val lines:List<InvoiceLine>,val discount:Double=0.0,val tax:Double=0.0,val paid:Double=0.0,val notes:String="")
class BusinessEngine(private val db:AppDatabase){
 suspend fun postInvoice(d:InvoiceDraft):Long{
  require(d.lines.isNotEmpty()){"أضف صنفًا واحدًا على الأقل"}
  require(d.exchangeRate>0){"سعر الصرف غير صحيح"}
  val subtotal=d.lines.sumOf{it.total};val total=(subtotal-d.discount+d.tax).coerceAtLeast(0.0)
  require(d.paid<=total){"المدفوع أكبر من الإجمالي"}
  return db.withTransaction{
   val id=db.invoices().insert(Invoice(number=d.number,type=d.type,dateMillis=System.currentTimeMillis(),contactId=d.contactId,warehouseId=d.warehouseId,currencyCode=d.currencyCode,exchangeRate=d.exchangeRate,subtotal=subtotal,discount=d.discount,tax=d.tax,total=total,paid=d.paid,notes=d.notes))
   db.invoiceLines().insertAll(d.lines.map{it.copy(invoiceId=id)})
   val salesAccount=db.accounts().byName(if(d.type=="SALE"||d.type=="SALE_RETURN")"المبيعات" else "المشتريات")
   val contactAccount=db.accounts().byName(if(d.type=="SALE"||d.type=="SALE_RETURN")"العملاء" else "الموردون")
   if(salesAccount!=null && contactAccount!=null){
    val sign=if(d.type=="SALE"||d.type=="PURCHASE")1.0 else -1.0
    val journalId=db.journals().entry(JournalEntry(referenceType=d.type,referenceId=id,dateMillis=System.currentTimeMillis(),memo="ترحيل فاتورة "+d.number))
    db.journals().lines(listOf(JournalLine(0,journalId,contactAccount.id,debit=if(sign>0)total else 0.0,credit=if(sign<0)total else 0.0),JournalLine(0,journalId,salesAccount.id,debit=if(sign<0)total else 0.0,credit=if(sign>0)total else 0.0)))
   }
   id
  }
 }
 suspend fun postReceipt(contactId:Long,cashAccountId:Long,amount:Double,reference:String){ val a=db.accounts().byName("العملاء") ?: error("حساب العملاء غير موجود"); AccountingEngine(db).postCash(a.id,cashAccountId,amount,"RECEIPT",reference) }
 suspend fun postPayment(contactId:Long,cashAccountId:Long,amount:Double,reference:String){ val a=db.accounts().byName("الموردون") ?: error("حساب الموردين غير موجود"); AccountingEngine(db).postCash(a.id,cashAccountId,amount,"PAYMENT",reference) }
}
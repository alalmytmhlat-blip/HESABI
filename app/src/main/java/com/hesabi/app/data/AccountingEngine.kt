package com.hesabi.app.data
import androidx.room.withTransaction
class AccountingEngine(private val db:AppDatabase){
 suspend fun postCash(accountId:Long,cashAccountId:Long,amount:Double,type:String,reference:String){
  require(amount>0){"المبلغ يجب أن يكون أكبر من صفر"}
  db.withTransaction{
   val entryId=db.journals().entry(JournalEntry(referenceType=type,referenceId=0,dateMillis=System.currentTimeMillis(),memo=reference))
   val debit=if(type=="RECEIPT") accountId else cashAccountId
   val credit=if(type=="RECEIPT") cashAccountId else accountId
   db.journals().lines(listOf(JournalLine(journalEntryId=entryId,accountId=debit,debit=amount),JournalLine(journalEntryId=entryId,accountId=credit,credit=amount)))
   db.cash().insert(CashTransaction(accountId=cashAccountId,type=type,dateMillis=System.currentTimeMillis(),amount=amount,reference=reference))
  }
 }
 suspend fun postJournal(referenceType:String,referenceId:Long,memo:String,lines:List<Pair<Long,Double>>){
  require(lines.isNotEmpty()){"القيد فارغ"}
  val debit=lines.filter{it.second>0}.sumOf{it.second};val credit=lines.filter{it.second<0}.sumOf{-it.second}
  require(kotlin.math.abs(debit-credit)<0.000001){"القيد غير متوازن"}
  db.withTransaction{val id=db.journals().entry(JournalEntry(referenceType,referenceId,System.currentTimeMillis(),memo));db.journals().lines(lines.map{if(it.second>=0)JournalLine(0,id,it.first,debit=it.second)else JournalLine(0,id,it.first,credit=-it.second)})}
 }
}
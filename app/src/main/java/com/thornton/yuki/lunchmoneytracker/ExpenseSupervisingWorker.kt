package com.thornton.yuki.lunchmoneytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.thornton.yuki.lunchmoneytracker.entity.Transaction
import com.thornton.yuki.lunchmoneytracker.helper.WorkHelper
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager
import java.time.LocalDate

private const val TAG = "LUNCH_DEV_EXP_SPV"

class ExpenseSupervisingWorker(context : Context, params : WorkerParameters): Worker(context, params) {

    private lateinit var storage: StorageManager

    override fun doWork(): Result {

        Log.d(TAG, "Worker Started")
        storage = StorageManager.getInstance(applicationContext)

        addTransactionAndSave()
        scheduleNext()

        return Result.success()
    }

    private fun addTransactionAndSave() {
        val transactions = storage.getTransactions()
        if (!transactions.none { LocalDate.now().isEqual(it.time.toLocalDate()) }) {
            Log.d(TAG, "Today's transaction found")
            return
        }
        val amount = storage.getDefaultDailyExpenseAmount()
        val transaction = Transaction(Transaction.Type.MINUS, amount)
        val total = storage.getBalance() - amount
        Log.d(TAG, "Changing balance to $total ; Adding a transaction: $transaction")
        transactions.add(transaction)
        storage.setTransactions(transactions)
        storage.setBalance(total)
    }

    private fun scheduleNext() {
        val supervisingSettings = storage.getExpenseSupervisingSchedule()
        if (supervisingSettings.activated) {
            val id = WorkHelper.scheduleTomorrow(ExpenseSupervisingWorker::class.java, supervisingSettings.time)
            storage.setExpenseSupervisingWorkerId(id)
        }
    }
}
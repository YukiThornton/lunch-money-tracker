package com.thornton.yuki.lunchmoneytracker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager
import com.thornton.yuki.lunchmoneytracker.entity.Transaction

class AddFundsActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_ADD_FUNDS_ACT"

    private val storage: StorageManager = StorageManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        findViewById<EditText>(R.id.new_balance).apply {
            setText("", TextView.BufferType.EDITABLE)
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addFundsAndReturnMain(getBalanceFromInput())
                    return@OnKeyListener true
                }
                false
            })
        }
        val fixedAmounts = arrayOf(2000, 2100, 2200)
        val buttonIds = arrayOf(R.id.fixed_amount_button1, R.id.fixed_amount_button2, R.id.fixed_amount_button3)
        for ((index, buttonId) in buttonIds.withIndex()) {
            findViewById<Button>(buttonId).apply {
                setText("+ ${fixedAmounts[index]}", TextView.BufferType.NORMAL)
                setOnClickListener{ addFundsAndReturnMain(fixedAmounts[index]) }
            }
        }
    }

    private fun addFundsAndReturnMain(amount: Int) {
        Log.d(tag, "Adding funds: +$amount")

        addAndSaveBalance(amount)

        val transaction = Transaction(Transaction.Type.PLUS, amount)
        addAndSaveTransaction(transaction)

        returnMain(true)
    }

    private fun addAndSaveBalance(amountToAdd: Int) {
        val total = amountToAdd + storage.getBalance()
        Log.d(tag, "Changing balance from ${storage.getBalance()} to $total")
        storage.setBalance(total)
    }

    private fun addAndSaveTransaction(transactionToAdd: Transaction) {
        val transactions = storage.getTransactions()
        transactions.add(transactionToAdd)
        storage.setTransactions(transactions)
    }

    private fun getBalanceFromInput() :Int {
        return Integer.parseInt(findViewById<EditText>(R.id.new_balance).text.toString())
    }

    private fun returnMain(hasNewEntry: Boolean) {
        val intent = Intent().apply {
            putExtra(INTENT_KEY_HAS_NEW_ENTRY, hasNewEntry)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

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
import android.widget.ImageButton
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.thornton.yuki.lunchmoneytracker.entity.Transaction
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager

private const val TAG = "LUNCH_DEV_ADD_EXP_ACT"

class AddExpenseActivity : AppCompatActivity() {

    private val optionButtonIds = arrayOf(R.id.option_1_button, R.id.option_2_button, R.id.option_3_button)
    private lateinit var storage: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = StorageManager.getInstance(applicationContext)
        setContentView(R.layout.activity_add_expense)

        findViewById<EditText>(R.id.custom_option_input).apply {
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addExpenseAndReturnMain(getBalanceFromInput())
                    return@OnKeyListener true
                }
                false
            })
        }
        updateOptionsInView()
        findViewById<ImageButton>(R.id.edit_expense_option_button).apply {
            setOnClickListener{ openEditOptionDialog() }
        }
    }

    private fun addExpenseAndReturnMain(amount: Int) {
        Log.d(TAG, "Adding expense: +$amount")

        subtractAndSaveBalance(amount)

        val transaction = Transaction(Transaction.Type.MINUS, amount)
        addAndSaveTransaction(transaction)

        returnMain(true)
    }

    private fun subtractAndSaveBalance(amountToSubtract: Int) {
        val total = storage.getBalance() - amountToSubtract
        Log.d(TAG, "Changing balance from ${storage.getBalance()} to $total")
        storage.setBalance(total)
    }

    private fun addAndSaveTransaction(transactionToAdd: Transaction) {
        val transactions = storage.getTransactions()
        transactions.add(transactionToAdd)
        storage.setTransactions(transactions)
    }

    private fun getBalanceFromInput() :Int {
        return Integer.parseInt(findViewById<EditText>(R.id.custom_option_input).text.toString())
    }

    private fun returnMain(hasNewEntry: Boolean) {
        val intent = Intent().apply {
            putExtra(INTENT_KEY_HAS_NEW_ENTRY, hasNewEntry)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun openEditOptionDialog() {
        val optionInputIds = listOf(R.id.option_1_input, R.id.option_2_input, R.id.option_3_input)
        val dialog = MaterialDialog(this)
            .title(R.string.edit_expense_option_dialog_title)
            .customView(R.layout.edit_expense_option_dialog)
            .positiveButton(R.string.edit_expense_option_dialog_positive) {
                val newOptionValues = optionInputIds.map { id ->
                    val editText = it.getCustomView()!!.findViewById<EditText>(id)
                    Integer.parseInt(editText.text.toString())
                }
                Log.d(TAG, "Updating expense option values: $newOptionValues")
                storage.setExpenseOptions(newOptionValues)
                updateOptionsInView()
            }
            .negativeButton(R.string.edit_expense_option_dialog_negative)
        val customView = dialog.getCustomView()
        val expenseOptions = storage.getExpenseOptions()
        optionInputIds.forEachIndexed { index, id ->
            customView!!.findViewById<EditText>(id).apply {
                setText(expenseOptions[index].toString())
            }
        }
        dialog.show()
    }

    private fun updateOptionsInView() {
        val expenseOptions = storage.getExpenseOptions()
        for ((index, buttonId) in optionButtonIds.withIndex()) {
            findViewById<Button>(buttonId).apply {
                setText("- ${expenseOptions[index]}", TextView.BufferType.NORMAL)
                setOnClickListener{ addExpenseAndReturnMain(expenseOptions[index]) }
            }
        }
    }
}

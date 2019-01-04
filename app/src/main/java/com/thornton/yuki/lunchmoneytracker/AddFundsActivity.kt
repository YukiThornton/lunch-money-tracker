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
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager
import com.thornton.yuki.lunchmoneytracker.entity.Transaction

class AddFundsActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_ADD_FUNDS_ACT"

    private val storage: StorageManager = StorageManager(this)
    private val optionButtonIds = arrayOf(R.id.option_1_button, R.id.option_2_button, R.id.option_3_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        findViewById<EditText>(R.id.custom_option_input).apply {
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addFundsAndReturnMain(getBalanceFromInput())
                    return@OnKeyListener true
                }
                false
            })
        }
        updateOptionsInView()
        findViewById<ImageButton>(R.id.edit_fund_option_button).apply {
            setOnClickListener{ openEditOptionDialog() }
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
            .title(R.string.edit_fund_option_dialog_title)
            .customView(R.layout.edit_fund_option_dialog)
            .positiveButton(R.string.edit_fund_option_dialog_positive) {
                val newOptionValues = optionInputIds.map { id ->
                    val editText = it.getCustomView()!!.findViewById<EditText>(id)
                    Integer.parseInt(editText.text.toString())
                }
                Log.d(tag, "Updating fund option values: $newOptionValues")
                storage.setFundOptions(newOptionValues)
                updateOptionsInView()
            }
            .negativeButton(R.string.edit_fund_option_dialog_negative)
        val customView = dialog.getCustomView()
        val fundOptions = storage.getFundOptions()
        optionInputIds.forEachIndexed { index, id ->
            customView!!.findViewById<EditText>(id).apply {
                setText(fundOptions[index].toString())
            }
        }
        dialog.show()
    }

    private fun updateOptionsInView() {
        val fundOptions = storage.getFundOptions()
        for ((index, buttonId) in optionButtonIds.withIndex()) {
            findViewById<Button>(buttonId).apply {
                setText("+ ${fundOptions[index]}", TextView.BufferType.NORMAL)
                setOnClickListener{ addFundsAndReturnMain(fundOptions[index]) }
            }
        }
    }
}

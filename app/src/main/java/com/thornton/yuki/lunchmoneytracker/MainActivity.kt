package com.thornton.yuki.lunchmoneytracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.thornton.yuki.lunchmoneytracker.storage.*
import com.thornton.yuki.lunchmoneytracker.entity.Transaction
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

const val INTENT_KEY_HAS_NEW_ENTRY = "HAS_NEW_ENTRY"

class MainActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_MAIN_ACT"
    private val activityCode = 1
    private val dateFormat = SimpleDateFormat("MM/DD(EEE) HH:mm")

    private val storage: StorageManager = StorageManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val balance = storage.getBalance()
        Log.d(tag, "onCreate: balance=$balance")

        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.balance).apply {
            setOnClickListener{ openEditBalanceDialog() }
        }
        findViewById<ImageButton>(R.id.edit_balance_button).apply {
            setOnClickListener{ openEditBalanceDialog() }
        }
        updateBalanceInView(balance)
        updateTransactionCards()
    }

    private fun openEditBalanceDialog() {
        val dialog = MaterialDialog(this)
            .title(R.string.edit_balance_dialog_title)
            .customView(R.layout.edit_balance_dialog)
            .positiveButton(R.string.edit_balance_dialog_positive) {
                val editText = it.getCustomView()!!.findViewById<EditText>(R.id.balance_input)
                val newBalance = Integer.parseInt(editText.text.toString())

                Log.d(tag, "Changing balance from ${storage.getBalance()} to $newBalance")

                storage.setBalance(newBalance)
                updateBalanceInView(newBalance)
            }
            .negativeButton(R.string.edit_balance_dialog_negative)
        val customView = dialog.getCustomView()
        customView!!.findViewById<EditText>(R.id.balance_input).apply {
            setText(storage.getBalance().toString())
        }
        dialog.show()
    }

    private fun updateBalanceInView(balance: Int) {
        findViewById<TextView>(R.id.balance).apply {
            text = balance.toString()
        }
    }

    fun onAddFundsClicked(view: View) {
        startAddFundsActivity()
    }

    private fun startAddFundsActivity() {
        Log.d(tag, "Starting AddFundsActivity")
        val intent = Intent(this, AddFundsActivity::class.java)
        startActivityForResult(intent, activityCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == activityCode && resultCode == Activity.RESULT_OK) {
            val safeIntent = data ?: intent
            if (safeIntent.getBooleanExtra(INTENT_KEY_HAS_NEW_ENTRY, false)) {
                refreshView()
            }
        }
    }

    private fun refreshView() {
        Log.d(tag, "Refreshing views")
        updateBalanceInView(storage.getBalance())
        updateTransactionCards()
    }

    private fun updateTransactionCards() {
        val container = findViewById<LinearLayout>(R.id.card_vertical_layout)
        container.removeAllViews()
        val transactions = storage.getTransactions()
        for (transaction in transactions) {
            createAndAddEntryCard(container, transaction)
        }
    }

    private fun createAndAddEntryCard(container: LinearLayout, transaction: Transaction) {
        val newCard = LayoutInflater.from(this).inflate(R.layout.money_entry_card, container, false)
        newCard.findViewById<TextView>(R.id.money).apply {
            text = transaction.amountWithSymbol()
        }
        newCard.findViewById<TextView>(R.id.date).apply {
            text = dateTimeText(transaction.calendar)
        }
        container.addView(newCard, 0)
    }

    private fun dateTimeText(cal: Calendar): String {
        return dateFormat.format(cal.time)
    }
}

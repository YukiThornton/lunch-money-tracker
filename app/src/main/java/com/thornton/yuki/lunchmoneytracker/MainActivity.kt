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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val INTENT_KEY_HAS_NEW_ENTRY = "HAS_NEW_ENTRY"
private const val TAG = "LUNCH_DEV_MAIN_ACT"
private const val TIME_FORMAT = "MM/DD(EEE) HH:mm"
private val TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT)

class MainActivity : AppCompatActivity() {

    private val addFundsActivityCode = 1
    private val addExpensesActivityCode = 2

    private lateinit var storage: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = StorageManager.getInstance(applicationContext)
        val balance = storage.getBalance()
        Log.d(TAG, "onCreate: balance=$balance")

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

                Log.d(TAG, "Changing balance from ${storage.getBalance()} to $newBalance")

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
        Log.d(TAG, "Starting AddFundsActivity")
        val intent = Intent(this, AddFundsActivity::class.java)
        startActivityForResult(intent, addFundsActivityCode)
    }

    fun onAddExpensesClicked(view: View) {
        startAddExpensesActivity()
    }

    private fun startAddExpensesActivity() {
        Log.d(TAG, "Starting AddExpenseActivity")
        val intent = Intent(this, AddExpenseActivity::class.java)
        startActivityForResult(intent, addExpensesActivityCode)
    }

    fun onPreferenceButtonClicked(view: View) {
        startPreferenceActivity()
    }

    private fun startPreferenceActivity() {
        Log.d(TAG, "Starting PreferenceActivity")
        val intent = Intent(this, PreferenceActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == addFundsActivityCode || requestCode == addExpensesActivityCode) && resultCode == Activity.RESULT_OK) {
            val safeIntent = data ?: intent
            if (safeIntent.getBooleanExtra(INTENT_KEY_HAS_NEW_ENTRY, false)) {
                refreshView()
            }
        }
    }

    private fun refreshView() {
        Log.d(TAG, "Refreshing views")
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
            text = dateTimeText(transaction.time)
        }
        container.addView(newCard, 0)
    }

    private fun dateTimeText(time: LocalDateTime): String {
        return time.format(TIME_FORMATTER)
    }
}

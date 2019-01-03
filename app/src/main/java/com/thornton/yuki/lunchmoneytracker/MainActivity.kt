package com.thornton.yuki.lunchmoneytracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.thornton.yuki.lunchmoneytracker.storage.*
import com.thornton.yuki.lunchmoneytracker.entity.Transaction

const val INTENT_KEY_HAS_NEW_ENTRY = "HAS_NEW_ENTRY"

class MainActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_MAIN_ACT"
    private val activityCode = 1
    private var currentBalance: Int = -1

    private val storage: StorageManager = StorageManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val balance = storage.getBalance()
        setBalance(balance)
        updateTransactionCards()
    }

    private fun setBalance(balance: Int) {
        Log.d(tag, "setBalance: value=$balance")
        currentBalance = balance
        val currentBalanceTextView = findViewById<TextView>(R.id.balance)
        currentBalanceTextView.text = currentBalance.toString()
    }

    fun onAddFundsClicked(view: View) {
        Log.d(tag, "onAddFundsClicked")
        startAddFundsActivity()
    }

    private fun startAddFundsActivity() {
        Log.d(tag, "startAddFundsActivity")
        val intent = Intent(this, AddFundsActivity::class.java)
        startActivityForResult(intent, activityCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(tag, "onActivityResult")
        if (requestCode == activityCode && resultCode == Activity.RESULT_OK) {
            val safeIntent = data ?: intent
            if (safeIntent.getBooleanExtra(INTENT_KEY_HAS_NEW_ENTRY, false)) {
                refreshView()
            }
        }
    }

    private fun refreshView() {
        setBalance(storage.getBalance())
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
        container.addView(newCard)
    }

    override fun onStop() {
        super.onStop()
        storage.setBalance(currentBalance)
    }
}

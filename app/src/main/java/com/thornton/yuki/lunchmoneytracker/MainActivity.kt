package com.thornton.yuki.lunchmoneytracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView

const val INTENT_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"
const val PREF_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"

class MainActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_MAIN_ACT"
    private val activityCode = 1
    private var currentBalance: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val balance = prefs.getInt(PREF_KEY_CURRENT_BALANCE, resources.getInteger(R.integer.initial_funds))
        setBalance(balance)
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
        val intent = Intent(this, AddFundsActivity::class.java).apply {
            putExtra(INTENT_KEY_CURRENT_BALANCE, currentBalance)
        }
        startActivityForResult(intent, activityCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(tag, "onActivityResult")
        if (requestCode == activityCode && resultCode == Activity.RESULT_OK) {
            val safeIntent = data ?: intent
            setBalance(safeIntent.getIntExtra(INTENT_KEY_CURRENT_BALANCE, currentBalance))
        }
    }

    override fun onStop() {
        super.onStop()
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putInt(PREF_KEY_CURRENT_BALANCE, currentBalance)
        editor.apply()
    }
}

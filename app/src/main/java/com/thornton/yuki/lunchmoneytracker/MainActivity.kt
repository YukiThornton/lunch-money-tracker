package com.thornton.yuki.lunchmoneytracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

const val INTENT_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"
const val PREF_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"

class MainActivity : AppCompatActivity() {

    private val tag = "DEV MAIN ACTIVITY"
    private var currentBalance: Int = -1

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val balance = prefs.getInt(PREF_KEY_CURRENT_BALANCE, resources.getInteger(R.integer.initial_funds))
        setBalance(balance)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun setBalance(balance: Int) {
        currentBalance = balance
        val currentBalanceTextView = findViewById<TextView>(R.id.balance)
        currentBalanceTextView.text = currentBalance.toString()
    }

    fun onAddFundsClicked(view: View) {
        Log.d(tag, "Add Funds Clicked")
        startAddFundsActivity()
    }

    private fun startAddFundsActivity() {
        val intent = Intent(this, AddFundsActivity::class.java).apply {
            putExtra(INTENT_KEY_CURRENT_BALANCE, currentBalance)
        }
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val safeIntent = intent ?: getIntent()
        setBalance(safeIntent.getIntExtra(INTENT_KEY_CURRENT_BALANCE, currentBalance))
    }

    override fun onStop() {
        super.onStop()
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putInt(PREF_KEY_CURRENT_BALANCE, currentBalance)
        editor.apply()
    }
}

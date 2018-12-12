package com.thornton.yuki.lunchmoneytracker

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class AddFundsActivity : AppCompatActivity() {

    private val tag = "LUNCH_DEV_ADD_FUNDS_ACT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        val currentBalance = intent.getIntExtra(INTENT_KEY_CURRENT_BALANCE, -1)
        findViewById<EditText>(R.id.new_balance).apply {
            setText(currentBalance.toString(), TextView.BufferType.EDITABLE)
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    changeBalanceAndReturnMain()
                    return@OnKeyListener true
                }
                false
            })
        }
        val fixedAmounts = arrayOf(2000, 2100, 2200)
        val buttonIds = arrayOf(R.id.fixed_amount_button1, R.id.fixed_amount_button2, R.id.fixed_amount_button3)
        for ((index, buttonId) in buttonIds.withIndex()) {
            findViewById<Button>(buttonId).apply {
                setText("+ ¥${fixedAmounts[index]}", TextView.BufferType.NORMAL)
                setOnClickListener{ addFundsAndReturnMain(fixedAmounts[index]) }
            }
        }
    }

    private fun addFundsAndReturnMain(amount: Int) {
        Log.d(tag, "addFundsAndReturnMain: funds amount=$amount")
        returnMain(getBalanceFromInput() + amount)
    }

    private fun changeBalanceAndReturnMain() {
        returnMain(getBalanceFromInput())
    }

    private fun getBalanceFromInput() :Int {
        return Integer.parseInt(findViewById<EditText>(R.id.new_balance).text.toString())
    }

    private fun returnMain(finalBalance: Int) {
        Log.d(tag, "returnMain: finalBalance=$finalBalance")
        val intent = Intent().apply {
            putExtra(INTENT_KEY_CURRENT_BALANCE, finalBalance)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

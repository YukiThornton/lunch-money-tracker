package com.thornton.yuki.lunchmoneytracker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView

class AddFundsActivity : AppCompatActivity() {

    private val tag = "DEV ADD FUNDS ACTIVITY"
    private var currentBalance: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        currentBalance = intent.getIntExtra(INTENT_KEY_CURRENT_BALANCE, -1)
        findViewById<EditText>(R.id.new_balance).apply {
            setText(currentBalance.toString(), TextView.BufferType.EDITABLE)
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    startMainActivity()
                    return@OnKeyListener true
                }
                false
            })
        }
    }

    private fun startMainActivity() {
        Log.d(tag, "Going back to MainActivity")
        currentBalance = Integer.parseInt(findViewById<EditText>(R.id.new_balance).text.toString())
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(INTENT_KEY_CURRENT_BALANCE, currentBalance)
        }
        startActivity(intent)
        finish()
    }
}

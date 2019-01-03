package com.thornton.yuki.lunchmoneytracker.storage

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thornton.yuki.lunchmoneytracker.entity.Transaction

const val PREF_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"
const val PREF_KEY_TRANSACTIONS = "TRANSACTIONS"
const val INITIAL_BALANCE = 1000

class StorageManager(private val context: Context) {

    fun getBalance(): Int {
        val manager = PreferenceManager.getDefaultSharedPreferences(this.context)
        return manager.getInt(PREF_KEY_CURRENT_BALANCE, INITIAL_BALANCE)
    }

    fun setBalance(newBalance: Int) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
        editor.putInt(PREF_KEY_CURRENT_BALANCE, newBalance)
        editor.apply()
    }

    fun getTransactions(): MutableList<Transaction> {
        val manager = PreferenceManager.getDefaultSharedPreferences(this.context)
        val gson = Gson()
        val transactionJson = manager.getString(PREF_KEY_TRANSACTIONS, gson.toJson(mutableListOf<Transaction>()))
        return gson.fromJson(transactionJson, object : TypeToken<MutableList<Transaction>>(){}.type)
    }

    fun setTransactions(transactions: List<Transaction>) {
        val gson = Gson()
        val editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit()
        editor.putString(PREF_KEY_TRANSACTIONS, gson.toJson(transactions))
        editor.apply()
    }
}
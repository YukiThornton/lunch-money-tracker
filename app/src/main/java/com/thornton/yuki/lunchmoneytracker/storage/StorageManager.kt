package com.thornton.yuki.lunchmoneytracker.storage

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thornton.yuki.lunchmoneytracker.R
import com.thornton.yuki.lunchmoneytracker.entity.Transaction

const val PREF_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"
const val PREF_KEY_TRANSACTIONS = "TRANSACTIONS"
val PREF_KEY_FUND_OPTIONS = listOf("FUND_OPTION_1", "FUND_OPTION_2", "FUND_OPTION_3")

class StorageManager(private val context: Context) {

    fun getBalance(): Int {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        return prefManager.getInt(PREF_KEY_CURRENT_BALANCE, context.resources.getInteger(R.integer.initial_balance))
    }

    fun setBalance(newBalance: Int) {
        val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefEditor.putInt(PREF_KEY_CURRENT_BALANCE, newBalance)
        prefEditor.apply()
    }

    fun getTransactions(): MutableList<Transaction> {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val transactionJson = prefManager.getString(PREF_KEY_TRANSACTIONS, gson.toJson(mutableListOf<Transaction>()))
        return gson.fromJson(transactionJson, object : TypeToken<MutableList<Transaction>>(){}.type)
    }

    fun setTransactions(transactions: List<Transaction>) {
        val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val gson = Gson()
        prefEditor.putString(PREF_KEY_TRANSACTIONS, gson.toJson(transactions))
        prefEditor.apply()
    }

    fun getFundOptions(): List<Int> {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        val initialFundOptionValues = listOf(
            context.resources.getInteger(R.integer.initial_fund_option_1),
            context.resources.getInteger(R.integer.initial_fund_option_2),
            context.resources.getInteger(R.integer.initial_fund_option_3)
        )

        return PREF_KEY_FUND_OPTIONS.mapIndexed{index, key -> prefManager.getInt(key, initialFundOptionValues[index])}
    }

    fun setFundOptions(newOptions: List<Int>) {
        if (newOptions.isEmpty() || newOptions.size != PREF_KEY_FUND_OPTIONS.size) {
            throw IllegalStateException("newOptions is empty or invalid size")
        }
        val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        PREF_KEY_FUND_OPTIONS.forEachIndexed { index, key ->  prefEditor.putInt(key, newOptions[index])}
        prefEditor.apply()
    }
}
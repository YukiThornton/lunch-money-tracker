package com.thornton.yuki.lunchmoneytracker.storage

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thornton.yuki.lunchmoneytracker.R
import com.thornton.yuki.lunchmoneytracker.entity.DailyWorkSchedule
import com.thornton.yuki.lunchmoneytracker.entity.Transaction
import java.time.LocalTime
import java.util.*

const val PREF_KEY_APP_BOOTED_ONCE = "APP_BOOTED_ONCE"
const val PREF_KEY_CURRENT_BALANCE = "CURRENT_BALANCE"
const val PREF_KEY_TRANSACTIONS = "TRANSACTIONS"
val PREF_KEY_FUND_OPTIONS = listOf("FUND_OPTION_1", "FUND_OPTION_2", "FUND_OPTION_3")
val PREF_KEY_EXPENSE_OPTIONS = listOf("EXPENSE_OPTION_1", "EXPENSE_OPTION_2", "EXPENSE_OPTION_3")
const val PREF_KEY_DAILY_EXP_SPV_SETTINGS = "DAILY_EXP_SPV_SETTINGS"
const val PREF_KEY_DEFAULT_DAILY_EXPENSE_AMOUNT = "DEFAULT_DAILY_EXPENSE_AMOUNT"
const val PREF_KEY_EXP_SPV_WORKER_ID = "EXP_SPV_WORKER_ID"

class StorageManager private constructor(private val context: Context) {

    private val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    private val gson = Gson()

    companion object {
        fun getInstance(context: Context): StorageManager {
            return StorageManager(context)
        }
    }

    fun appBootedOnce(): Boolean {
        return prefManager.getBoolean(PREF_KEY_APP_BOOTED_ONCE, false)
    }

    fun setAppBootedOnceTrue() {
        prefEditor.putBoolean(PREF_KEY_APP_BOOTED_ONCE, true)
        prefEditor.apply()
    }

    fun getBalance(): Int {
        return prefManager.getInt(PREF_KEY_CURRENT_BALANCE, resInt(R.integer.initial_balance))
    }

    fun setBalance(newBalance: Int) {
        prefEditor.putInt(PREF_KEY_CURRENT_BALANCE, newBalance)
        prefEditor.apply()
    }

    fun getTransactions(): MutableList<Transaction> {
        val transactionJson = prefManager.getString(PREF_KEY_TRANSACTIONS, gson.toJson(mutableListOf<Transaction>()))
        return gson.fromJson(transactionJson, object : TypeToken<MutableList<Transaction>>(){}.type)
    }

    fun setTransactions(transactions: List<Transaction>) {
        prefEditor.putString(PREF_KEY_TRANSACTIONS, gson.toJson(transactions))
        prefEditor.apply()
    }

    fun getFundOptions(): List<Int> {
        val initialFundOptionValues = listOf(
            resInt(R.integer.initial_fund_option_1),
            resInt(R.integer.initial_fund_option_2),
            resInt(R.integer.initial_fund_option_3)
        )

        return PREF_KEY_FUND_OPTIONS.mapIndexed{index, key -> prefManager.getInt(key, initialFundOptionValues[index])}
    }

    fun setFundOptions(newOptions: List<Int>) {
        if (newOptions.isEmpty() || newOptions.size != PREF_KEY_FUND_OPTIONS.size) {
            throw IllegalStateException("newOptions is empty or invalid size")
        }
        PREF_KEY_FUND_OPTIONS.forEachIndexed { index, key ->  prefEditor.putInt(key, newOptions[index])}
        prefEditor.apply()
    }

    fun getExpenseOptions(): List<Int> {
        val initialExpenseOptionValues = listOf(
            resInt(R.integer.initial_expense_option_1),
            resInt(R.integer.initial_expense_option_2),
            resInt(R.integer.initial_expense_option_3)
        )

        return PREF_KEY_EXPENSE_OPTIONS.mapIndexed{index, key -> prefManager.getInt(key, initialExpenseOptionValues[index])}
    }

    fun setExpenseOptions(newOptions: List<Int>) {
        if (newOptions.isEmpty() || newOptions.size != PREF_KEY_EXPENSE_OPTIONS.size) {
            throw IllegalStateException("newOptions is empty or invalid size")
        }
        PREF_KEY_EXPENSE_OPTIONS.forEachIndexed { index, key ->  prefEditor.putInt(key, newOptions[index])}
        prefEditor.apply()
    }

    fun getExpenseSupervisingSchedule(): DailyWorkSchedule {
        val defaultSettings = getDefaultExpenseSupervisingSettings()
        val settingsJson = prefManager.getString(PREF_KEY_DAILY_EXP_SPV_SETTINGS, gson.toJson(defaultSettings))
        return gson.fromJson(settingsJson, object : TypeToken<DailyWorkSchedule>(){}.type)
    }

    private fun getDefaultExpenseSupervisingSettings(): DailyWorkSchedule {
        val defaultTime = LocalTime.of(resInt(R.integer.initial_exp_spv_hour),resInt(R.integer.initial_exp_spv_minute))
        return DailyWorkSchedule.of(resBool(R.bool.run_daily_exp_spv), defaultTime)
    }

    fun setExpenseSupervisingSchedule(settings: DailyWorkSchedule) {
        prefEditor.putString(PREF_KEY_DAILY_EXP_SPV_SETTINGS, gson.toJson(settings))
        prefEditor.apply()
    }

    fun getDefaultDailyExpenseAmount(): Int {
        return prefManager.getInt(PREF_KEY_DEFAULT_DAILY_EXPENSE_AMOUNT, resInt(R.integer.initial_default_daily_expense_amount))
    }

    fun setDefaultDailyExpenseAmount(amount: Int) {
        prefEditor.putInt(PREF_KEY_DEFAULT_DAILY_EXPENSE_AMOUNT, amount)
        prefEditor.apply()
    }

    private fun resInt(resourceId: Int): Int {
        return context.resources.getInteger(resourceId)
    }

    private fun resBool(resourceId: Int): Boolean {
        return context.resources.getBoolean(resourceId)
    }

    fun getExpenseSupervisingWorkerId(): UUID? {
        val uuidJson = prefManager.getString(PREF_KEY_EXP_SPV_WORKER_ID, null)
        return gson.fromJson(uuidJson, object : TypeToken<UUID>(){}.type)
    }

    fun setExpenseSupervisingWorkerId(newId: UUID) {
        prefEditor.putString(PREF_KEY_EXP_SPV_WORKER_ID, gson.toJson(newId))
        prefEditor.apply()
    }

    fun removeExpenseSupervisingWorkerId() {
        prefEditor.remove(PREF_KEY_EXP_SPV_WORKER_ID)
        prefEditor.commit()
    }
}
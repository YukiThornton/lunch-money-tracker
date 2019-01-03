package com.thornton.yuki.lunchmoneytracker.entity

import java.util.*

data class Transaction (
    val type: Type,
    val amount: Int,
    val calendar: Calendar = Calendar.getInstance()
) {
    enum class Type(val symbol: String) {
        PLUS("+"),
        MINUS("-")
    }

    fun amountWithSymbol(): String {
        return "${type.symbol}$amount"
    }

    override fun toString(): String {
        return "${calendar.get(Calendar.DAY_OF_MONTH)}: ${amountWithSymbol()}"
    }
}
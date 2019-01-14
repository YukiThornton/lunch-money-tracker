package com.thornton.yuki.lunchmoneytracker.entity

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME

data class Transaction (
    val type: Type,
    val amount: Int,
    val time: LocalDateTime = LocalDateTime.now()
) {
    enum class Type(val symbol: String) {
        PLUS("+"),
        MINUS("-")
    }

    fun amountWithSymbol(): String {
        return "${type.symbol}$amount"
    }

    override fun toString(): String {
        return "${time.format(TIME_FORMATTER)}: ${amountWithSymbol()}"
    }
}
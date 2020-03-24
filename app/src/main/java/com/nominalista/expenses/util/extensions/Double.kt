package com.nominalista.expenses.util.extensions

import com.nominalista.expenses.data.model.Currency
import java.text.NumberFormat

fun Double.toExactFloat() = toString().toFloat()
fun Double.toMoneyString(currency: Currency): String {
    return when {
        currency.locale != null -> useLocale(currency, this)
        else -> {
            val amount = "%.2f".format(this)
            val symbol = currency.symbol
            "$amount $symbol"
        }
    }
}

private fun useLocale(currency: Currency, amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(currency.locale)
    format.maximumFractionDigits = 0
    return format.format(amount)
}
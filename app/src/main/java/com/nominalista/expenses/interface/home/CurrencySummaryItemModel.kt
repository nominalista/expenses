package com.nominalista.expenses.`interface`.home

import com.nominalista.expenses.data.Currency

class CurrencySummaryItemModel(currency: Currency, amount: Float) {

    val amount = "${"%.2f".format(amount)} ${currency.symbol}"
    val currency = "(${currency.title} • ${currency.code})"
}
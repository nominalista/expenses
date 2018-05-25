package com.nominalista.expenses.ui.home

import com.nominalista.expenses.model.Currency

class CurrencySummaryItemModel(currency: Currency, amount: Float) {

    val amount = "${"%.2f".format(amount)} ${currency.symbol}"
    val currency = "(${currency.title} • ${currency.code})"
}
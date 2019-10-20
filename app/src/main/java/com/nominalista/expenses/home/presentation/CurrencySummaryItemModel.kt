package com.nominalista.expenses.home.presentation

import com.nominalista.expenses.data.model.Currency

data class CurrencySummaryItemModel(val currency: Currency, val amount: Double) {
    val amountText by lazy { "${"%.2f".format(amount)} ${currency.symbol}" }
    val currencyText by lazy { "(${currency.title} • ${currency.code})" }
}
package com.nominalista.expenses.home.presentation

import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.util.extensions.toMoneyString

data class CurrencySummaryItemModel(val currency: Currency, val amount: Double) {
    val amountText by lazy { amount.toMoneyString(currency) }
    val currencyText by lazy { "(${currency.title} • ${currency.code})" }
}
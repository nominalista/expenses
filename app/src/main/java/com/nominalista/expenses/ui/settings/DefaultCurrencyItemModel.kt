package com.nominalista.expenses.ui.settings

import com.nominalista.expenses.model.Currency

class DefaultCurrencyItemModel(val currency: Currency) : SettingItemModel {

    val flag = currency.flag
    val subtitle = "${currency.title} (${currency.code})"

    var click: (() -> Unit)? = null
}
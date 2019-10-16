package com.nominalista.expenses.settings.presentation

class SummaryActionSettingItemModel(val title: String, val summary: String) : SettingItemModel {
    var click: (() -> Unit)? = null
}
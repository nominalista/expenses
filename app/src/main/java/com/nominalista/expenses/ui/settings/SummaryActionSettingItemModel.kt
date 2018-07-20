package com.nominalista.expenses.ui.settings

class SummaryActionSettingItemModel(val title: String, val summary: String) : SettingItemModel {

    var click: (() -> Unit)? = null
}
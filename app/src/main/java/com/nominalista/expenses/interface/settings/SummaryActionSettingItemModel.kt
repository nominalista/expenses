package com.nominalista.expenses.`interface`.settings

class SummaryActionSettingItemModel(val title: String, val summary: String) : SettingItemModel {

    var click: (() -> Unit)? = null
}
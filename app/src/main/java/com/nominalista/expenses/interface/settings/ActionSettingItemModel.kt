package com.nominalista.expenses.`interface`.settings

open class ActionSettingItemModel(val title: String): SettingItemModel {

    var click: (() -> Unit)? = null
}
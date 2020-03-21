package com.nominalista.expenses.settings.presentation

class SwitchSettingItemModel(val title: String, val enabled: Boolean) : SettingItemModel {
    var isChecked: ((checked: Boolean) -> Unit)? = null
}
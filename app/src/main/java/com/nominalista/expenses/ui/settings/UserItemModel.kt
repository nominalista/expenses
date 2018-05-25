package com.nominalista.expenses.ui.settings

import com.nominalista.expenses.model.User

class UserItemModel(val user: User): SettingItemModel {

    val name = user.name
    var deleteButtonClick: (() -> Unit)? = null
}
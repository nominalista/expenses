package com.nominalista.expenses.userinterface.newexpense.tagselection

import com.nominalista.expenses.data.Tag

class TagItemModel(val tag: Tag): TagSelectionItemModel {

    var isChecked = false
    val name = tag.name

    var checkClick: (() -> Unit)? = null
    var deleteClick: (() -> Unit)? = null
}
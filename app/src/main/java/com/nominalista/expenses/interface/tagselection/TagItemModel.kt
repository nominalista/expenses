package com.nominalista.expenses.`interface`.tagselection

import com.nominalista.expenses.data.Tag

class TagItemModel(val tag: Tag): TagSelectionItemModel {

    var isSelected = false
    val name = tag.name

    var selectClick: (() -> Unit)? = null
    var removeClick: (() -> Unit)? = null
}
package com.nominalista.expenses.userinterface.newexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.Variable

class NewExpenseActivityModel: ViewModel() {

    val selectedTags = Variable(emptyList<Tag>())

    fun selectTags(tags: List<Tag>) {
        selectedTags.value = tags
    }
}
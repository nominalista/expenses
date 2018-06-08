package com.nominalista.expenses.`interface`.tagselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.infrastructure.utils.Variable

class NewTagDialogModel(private val databaseDataSource: DatabaseDataSource) : ViewModel() {

    val isAddEnabled = Variable(false)

    private var name = ""

    fun updateName(name: String) {
        this.name = name
        isAddEnabled.value = name.isNotEmpty()
    }

    fun addTag() {
        val tag = Tag(0, name)
        databaseDataSource.insertTag(tag)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            return NewTagDialogModel(databaseDataSource) as T
        }
    }
}
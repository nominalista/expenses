package com.nominalista.expenses.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.infrastructure.utils.runOnBackground
import com.nominalista.expenses.model.ApplicationDatabase
import com.nominalista.expenses.model.User

class NewUserDialogModel(private val database: ApplicationDatabase) : ViewModel() {

    var name: String? = null
    val isAddEnabled = Variable(false)

    fun updateName(name: String?) {
        this.name = name
        isAddEnabled.value = name != null && name.isNotEmpty()
    }

    fun addUser() {
        val user = User(name ?: "No name")
        runOnBackground { database.userDao().insert(user) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NewUserDialogModel(application.database) as T
        }
    }
}
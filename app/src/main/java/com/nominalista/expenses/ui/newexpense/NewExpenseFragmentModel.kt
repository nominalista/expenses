package com.nominalista.expenses.ui.newexpense

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.infrastructure.extensions.truncatedTime
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.infrastructure.utils.runOnBackground
import com.nominalista.expenses.model.Currency
import com.nominalista.expenses.model.Expense
import com.nominalista.expenses.model.User
import com.nominalista.expenses.source.PreferenceDataSource
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class NewExpenseFragmentModel(application: Application) : AndroidViewModel(application) {

    val selectedCurrency: Variable<Currency>
    val selectedUser: Variable<User>
    val selectedDate: Variable<Date>

    var users = emptyList<User>()
    val isUserSelectionEnabled get() = users.isNotEmpty()

    val finish = Event()

    private var amount = 0f
    private var title = ""
    private var notes = ""

    private val database = application.database
    private val preferenceDataSource = PreferenceDataSource()
    private val compositeDisposable = CompositeDisposable()

    init {
        selectedCurrency = Variable(getDefaultCurrency())
        selectedUser = Variable(getDefaultUser())
        selectedDate = Variable(getDefaultDate())
        loadUsers()
    }

    private fun getDefaultCurrency(): Currency {
        val context = getApplication<Application>()
        return preferenceDataSource.getDefaultCurrency(context)
    }

    private fun getDefaultUser(): User {
        return User("No user")
    }

    private fun getDefaultDate(): Date {
        return Calendar.getInstance().truncatedTime
    }

    private fun loadUsers() {
        compositeDisposable += database.userDao()
                .getAll()
                .subscribe { users = it }
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeDatabase()
    }

    private fun unsubscribeDatabase() {
        compositeDisposable.dispose()
    }

    // Selection

    fun selectCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    fun selectUser(user: User) {
        selectedUser.value = user
    }

    fun selectDate(date: Date) {
        selectedDate.value = date
    }

    // Updating

    fun updateAmount(amount: Float) {
        this.amount = amount
    }

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateNotes(notes: String) {
        this.notes = notes
    }

    // Creating

    fun createExpense() {
        val currency = selectedCurrency.value
        val userName = selectedUser.value.name
        val date = selectedDate.value
        val expense = Expense(null, amount, currency, title, userName, date, notes)
        runOnBackground { database.expenseDao().insert(expense) }
        finish.next()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NewExpenseFragmentModel(application) as T
        }
    }
}
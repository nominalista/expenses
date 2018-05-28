package com.nominalista.expenses.ui.newexpense

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.infrastructure.extensions.truncatedTime
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.source.PreferenceDataSource
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class NewExpenseFragmentModel(
        application: Application,
        private val databaseDataSource: DatabaseDataSource,
        private val preferenceDataSource: PreferenceDataSource
) : AndroidViewModel(application) {

    val selectedCurrency: Variable<Currency>
    val selectedDate: Variable<Date>
    val selectedTags: Variable<List<Tag>>
    val finish = Event()

    private var amount = 0f
    private var title = ""
    private var notes = ""
    private val compositeDisposable = CompositeDisposable()

    init {
        selectedCurrency = Variable(getDefaultCurrency())
        selectedDate = Variable(getDefaultDate())
        selectedTags = Variable(getDefaultTags())
    }

    private fun getDefaultCurrency(): Currency {
        val context = getApplication<Application>()
        return preferenceDataSource.getDefaultCurrency(context)
    }

    private fun getDefaultDate() = Calendar.getInstance().truncatedTime

    private fun getDefaultTags() = emptyList<Tag>()

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

    fun selectDate(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        selectedDate.value = calendar.time
    }

    fun selectTags(tags: List<Tag>) {
        selectedTags.value = tags
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
        val date = selectedDate.value
        val tags = selectedTags.value
        val expense = Expense(0, amount, currency, title, date, notes, tags)
        databaseDataSource.insertExpense(expense)
        finish.next()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            val preferenceDataSource = PreferenceDataSource()
            return NewExpenseFragmentModel(application,
                    databaseDataSource,
                    preferenceDataSource) as T
        }
    }
}
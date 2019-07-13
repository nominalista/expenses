package com.nominalista.expenses.addeditexpense.presentation

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io

class AddEditExpenseFragmentModel(
    application: Application,
    private val databaseDataSource: DatabaseDataSource,
    private val preferenceDataSource: PreferenceDataSource,
    private val expense: Expense?
) : AndroidViewModel(application) {

    var amount: Float? = null
    var title: String = ""
    var notes: String = ""

    val selectedCurrency = Variable(Currency.USD)
    val selectedDate = Variable(Date.now())
    val selectedTags = Variable(emptyList<Tag>())

    val finish = Event()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        if (expense == null) {
            setDefaultCurrency()
        } else {
            populateData(expense)
        }
    }

    private fun setDefaultCurrency() {
        getApplication<Application>().let {
            selectedCurrency.value = preferenceDataSource.getDefaultCurrency(it)
        }
    }

    private fun populateData(expense: Expense) {
        amount = expense.amount
        title = expense.title
        notes = expense.notes

        selectedCurrency.value = expense.currency
        selectedDate.value = expense.date
        selectedTags.value = expense.tags
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    // Selection

    fun selectCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    fun selectTags(tags: List<Tag>) {
        selectedTags.value = tags
    }

    fun selectDate(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val date = Date.from(year, month, day, hour, minute, 0)
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

    // Saving

    fun saveExpense() {
        if (expense == null) {
            createExpense()
        } else {
            updateExpense(expense)
        }
    }

    private fun createExpense() {
        val expenseForInsertion = prepareExpenseForInsertion()
        disposables += databaseDataSource.insertExpense(expenseForInsertion)
            .subscribeOn(io())
            .observeOn(mainThread())
            .doOnTerminate { finish.next() }
            .subscribe({ id ->
                Log.d(TAG, "Expense insertion succeeded, id: $id.")
            }, { error ->
                Log.e(TAG, "Expense insertion failed (${error.message}).")
            })
    }

    private fun prepareExpenseForInsertion(): Expense {
        val amount = amount ?: 0f
        val currency = selectedCurrency.value
        val date = selectedDate.value
        val tags = selectedTags.value
        return Expense(0, amount, currency, title, date, notes, tags)
    }

    private fun updateExpense(expense: Expense) {
        val expenseForUpdate = prepareExpenseForUpdate(expense)
        disposables += databaseDataSource.updateExpense(expenseForUpdate)
            .subscribeOn(io())
            .observeOn(mainThread())
            .doOnTerminate { finish.next() }
            .subscribe({
                Log.d(TAG, "Expense update succeeded.")
            }, { error ->
                Log.d(TAG, "Expense update failed (${error.message}.")
            })
    }

    private fun prepareExpenseForUpdate(expense: Expense): Expense {
        val amount = amount ?: 0f
        val currency = selectedCurrency.value
        val date = selectedDate.value
        val tags = selectedTags.value
        return expense.copy(
            amount = amount,
            currency = currency,
            title = title,
            date = date,
            notes = notes,
            tags = tags
        )
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val expense: Expense?
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            val preferenceDataSource = PreferenceDataSource()

            return AddEditExpenseFragmentModel(
                application,
                databaseDataSource,
                preferenceDataSource,
                expense
            ) as T
        }
    }

    companion object {
        private val TAG = AddEditExpenseFragmentModel::class.java.simpleName
    }
}
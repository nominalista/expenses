package com.nominalista.expenses.addeditexpense.presentation

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io
import org.threeten.bp.LocalDate

class AddEditExpenseFragmentModel(
    application: Application,
    private val dataStore: DataStore,
    private val preferenceDataSource: PreferenceDataSource,
    private val expense: Expense?
) : AndroidViewModel(application) {

    var amount: Double? = null
    var title: String = ""
    var notes: String = ""

    val selectedCurrency = Variable(Currency.USD)
    val selectedDate = Variable(LocalDate.now())
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

    fun selectDate(year: Int, month: Int, day: Int) {
        selectedDate.value = LocalDate.of(year, month, day)
    }

    // Updating

    fun updateAmount(amount: Double) {
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

        disposables += dataStore.insertExpense(expenseForInsertion)
            .subscribeOn(io())
            .observeOn(mainThread())
            .doOnTerminate { finish.next() }
            .subscribe({
                Log.d(TAG, "Expense insertion succeeded.")
            }, { error ->
                Log.e(TAG, "Expense insertion failed (${error.message}).")
            })
    }

    private fun prepareExpenseForInsertion(): Expense {
        return Expense(
            "",
            amount ?: 0.0,
            selectedCurrency.value,
            title,
            selectedTags.value,
            selectedDate.value,
            notes,
            null
        )
    }

    private fun updateExpense(expense: Expense) {
        val expenseForUpdate = prepareExpenseForUpdate(expense)

        disposables += dataStore.updateExpense(expenseForUpdate)
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
        return expense.copy(
            amount = amount ?: 0.0,
            currency = selectedCurrency.value,
            title = title,
            tags = selectedTags.value,
            date = selectedDate.value,
            notes = notes
        )
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val expense: Expense?
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddEditExpenseFragmentModel(
                application,
                application.defaultDataStore,
                application.preferenceDataSource,
                expense
            ) as T
        }
    }

    companion object {
        private val TAG = AddEditExpenseFragmentModel::class.java.simpleName
    }
}
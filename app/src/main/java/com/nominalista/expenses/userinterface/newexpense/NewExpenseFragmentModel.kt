package com.nominalista.expenses.userinterface.newexpense

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.automaton.ApplicationAutomaton
import com.nominalista.expenses.automaton.newexpense.NewExpenseInputs.*
import com.nominalista.expenses.automaton.newexpense.NewExpenseState
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.disposables.Disposable

class NewExpenseFragmentModel(
        application: Application,
        private val automaton: ApplicationAutomaton
) : AndroidViewModel(application) {

    val selectedCurrency = Variable(Currency.USD)
    val selectedDate = Variable(Date.now())
    val selectedTags = Variable(emptyList<Tag>())
    val finish = Event()

    private var amount = 0f
    private var title = ""
    private var notes = ""

    private var automatonDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeAutomaton()
        sendLoadDefaultCurrency(getApplication())
        sendSetSelectedDate(Date.now())
    }

    private fun subscribeAutomaton() {
        automatonDisposable = automaton.state
                .map { it.newExpenseState }
                .distinctUntilChanged()
                .subscribe { stateChanged(it) }
    }

    private fun stateChanged(state: NewExpenseState) {
        selectedCurrency.value = state.selectedCurrency
        selectedDate.value = state.selectedDate
        selectedTags.value = state.selectedTags
        amount = state.amount
        title = state.title
        notes = state.notes
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        unsubscribeAutomaton()
        sendRestoreState()
    }

    private fun unsubscribeAutomaton() {
        automatonDisposable?.dispose()
        automatonDisposable = null
    }

    // Selection

    fun selectCurrency(currency: Currency) {
        sendSetSelectedCurrency(currency)
    }

    fun selectDate(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val date = Date.from(year, month, day, hour, minute, 0)
        sendSetSelectedDate(date)
    }

    // Updating

    fun updateAmount(amount: Float) {
        sendSetAmount(amount)
    }

    fun updateTitle(title: String) {
        sendSetTitle(title)
    }

    fun updateNotes(notes: String) {
        sendSetNotes(notes)
    }

    // Creating

    fun createExpense() {
        val currency = selectedCurrency.value
        val date = selectedDate.value
        val tags = selectedTags.value
        val expense = Expense(0, amount, currency, title, date, notes, tags.toList())
        sendCreateExpense(expense)
        finish.next()
    }

    // Sending inputs

    private fun sendSetSelectedCurrency(selectedCurrency: Currency) =
            automaton.send(SetSelectedCurrencyInput(selectedCurrency))

    private fun sendSetSelectedDate(selectedDate: Date) =
            automaton.send(SetSelectedDateInput(selectedDate))

    private fun sendSetAmount(amount: Float) = automaton.send(SetAmountInput(amount))

    private fun sendSetTitle(title: String) = automaton.send(SetTitleInput(title))

    private fun sendSetNotes(notes: String) = automaton.send(SetNotesInput(notes))

    private fun sendLoadDefaultCurrency(context: Context) =
            automaton.send(LoadDefaultCurrencyInput(context))

    private fun sendCreateExpense(expense: Expense) = automaton.send(CreateExpenseInput(expense))

    private fun sendRestoreState() = automaton.send(RestoreStateInput)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val automaton = application.automaton
            return NewExpenseFragmentModel(application, automaton) as T
        }
    }
}
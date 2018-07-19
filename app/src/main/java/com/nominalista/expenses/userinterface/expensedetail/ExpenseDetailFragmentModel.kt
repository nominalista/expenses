package com.nominalista.expenses.userinterface.expensedetail

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.automaton.ApplicationAutomaton
import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailInput.*
import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailState
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.disposables.Disposable

class ExpenseDetailFragmentModel(
        application: Application,
        private val automaton: ApplicationAutomaton
) : AndroidViewModel(application) {

    val amount = Variable("")
    val currency = Variable("")
    val title = Variable("")
    val tags = Variable(emptyList<Tag>())
    val date = Variable("")
    val notes = Variable("")

    val finish = Event()

    private var automatonDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeAutomaton()
    }

    private fun subscribeAutomaton() {
        automatonDisposable = automaton.state
                .map { it.expenseDetailState }
                .distinctUntilChanged()
                .subscribe { stateChanged(it) }
    }

    private fun stateChanged(expenseDetailState: ExpenseDetailState) {
        val expense = expenseDetailState.expense ?: return
        amount.value = "${"%.2f".format(expense.amount)} ${expense.currency.symbol}"
        currency.value = "(${expense.currency.title} • ${expense.currency.code})"
        title.value = expense.title
        tags.value = expense.tags
        date.value = createDate(expense)
        notes.value = createNotes(expense)
    }

    private fun createDate(expense: Expense) = expense.date.toReadableString()

    private fun createNotes(expense: Expense): String {
        val notes = expense.notes
        return if (notes.isNotEmpty()) notes
        else getApplication<Application>().getString(R.string.no_notes)
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

    // Public

    fun delete() {
        val expense = automaton.state.value.expenseDetailState.expense ?: return
        sendDeleteExpense(expense)
        finish.next()
    }

    // Sending inputs

    private fun sendDeleteExpense(expense: Expense) = automaton.send(DeleteExpenseInput(expense))

    private fun sendRestoreState() = automaton.send(RestoreStateInput)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val automaton = application.automaton
            return ExpenseDetailFragmentModel(application, automaton) as T
        }
    }
}
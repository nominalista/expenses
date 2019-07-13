package com.nominalista.expenses.expensedetail.presentation

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.expensedetail.domain.DeleteExpenseUseCase
import com.nominalista.expenses.expensedetail.domain.ObserveExpenseUseCase
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io

class ExpenseDetailFragmentModel(
    application: Application,
    private val observeExpenseUseCase: ObserveExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private var expense: Expense
) : AndroidViewModel(application) {

    val amount = Variable("")
    val currency = Variable("")
    val title = Variable("")
    val tags = Variable(emptyList<Tag>())
    val date = Variable("")
    val notes = Variable("")

    val showEdit = DataEvent<Expense>()
    val finish = Event()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        observeExpense()
    }

    private fun observeExpense() {
        disposables += observeExpenseUseCase(expense.id)
            .subscribeOn(io())
            .observeOn(mainThread())
            .subscribe { expense = it; populateExpenseValues() }
    }

    private fun populateExpenseValues() {
        amount.value = "${"%.2f".format(expense.amount)} ${expense.currency.symbol}"
        currency.value = "(${expense.currency.title} • ${expense.currency.code})"
        title.value = expense.title
        tags.value = expense.tags
        date.value = expense.date.toReadableString()
        notes.value = makeNotes(expense)
    }

    private fun makeNotes(expense: Expense): String {
        val notes = expense.notes
        return if (notes.isNotEmpty()) notes
        else getApplication<Application>().getString(R.string.no_notes)
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    // Public

    fun edit() {
        showEdit.next(expense)
    }

    fun delete() {
        disposables += deleteExpenseUseCase(expense)
            .subscribeOn(io())
            .observeOn(mainThread())
            .subscribe { finish.next() }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val expense: Expense) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)

            val observeExpenseUseCase = ObserveExpenseUseCase(databaseDataSource)
            val deleteExpenseUseCase = DeleteExpenseUseCase(databaseDataSource)

            return ExpenseDetailFragmentModel(
                application,
                observeExpenseUseCase,
                deleteExpenseUseCase,
                expense
            ) as T
        }
    }
}
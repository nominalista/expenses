package com.nominalista.expenses.expensehistory.presentation

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.home.domain.FilterExpensesUseCase
import com.nominalista.expenses.home.domain.SortExpensesUseCase
import com.nominalista.expenses.home.presentation.ExpenseItemModel
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.computation
import io.reactivex.schedulers.Schedulers.io
import org.threeten.bp.LocalDate

class ExpenseFragmentModel(
        application: Application,
        private val dataStore: DataStore
) : AndroidViewModel(application) {

    val expenseItemModels = Variable(emptyList<ExpenseItemModel>())
    val isLoading = Variable(false)
    val showExpenseDetail = DataEvent<Expense>()

    var expenses = emptyList<Expense>()
    var tags = emptyList<Tag>()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        observeExpenses()
        updateExpenseItemModels(LocalDate.now())
    }


    private fun observeExpenses() {
        disposables += dataStore.observeExpenses()
                .map { SortExpensesUseCase().invoke(it) }
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe { expenses = it; updateExpenseItemModels(LocalDate.now()) }
    }

    private fun updateExpenseItemModels(date: LocalDate) {
        disposables += Observable.just(expenses)
                .map { FilterExpensesUseCase().invoke(it, date) }
                .map { createExpenseSection(it) }
                .subscribeOn(computation())
                .observeOn(mainThread())
                .subscribe { expenseItemModels.value = it }
    }

    private fun createExpenseSection(expenses: List<Expense>): List<ExpenseItemModel> {
        return expenses.map { expense -> createExpenseItemModel(expense) }
    }

    private fun createExpenseItemModel(expense: Expense): ExpenseItemModel {
        val itemModel = ExpenseItemModel(expense)
        itemModel.click = { showExpenseDetail.next(expense) }
        return itemModel
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    // Public

    fun showExpensesForTheDay(date: LocalDate) {
        disposables += Observable.just(expenses)
                .map { FilterExpensesUseCase().invoke(it, date) }
                .map { createExpenseSection(it) }
                .subscribeOn(computation())
                .observeOn(mainThread())
                .subscribe { expenseItemModels.value = it }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ExpenseFragmentModel(
                    application,
                    application.defaultDataStore
            ) as T
        }
    }
}

package com.nominalista.expenses.ui.home

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.infrastructure.utils.DataEvent
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.model.Currency
import com.nominalista.expenses.model.DateRange
import com.nominalista.expenses.model.Expense
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeFragmentModel(application: Application) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<HomeItemModel>())
    val showExpenseDetail = DataEvent<Expense>()

    private val database = application.database
    private var dateRange = DateRange.AllTime
    private var itemModelsDisposable: Disposable? = null

    init {
        subscribeItemModels()
    }

    private fun subscribeItemModels() {
        itemModelsDisposable = getItemModels().subscribe { itemModels.value = it }
    }

    private fun getItemModels(): Flowable<List<HomeItemModel>> {
        return getExpenses()
                .observeOn(Schedulers.io())
                .map { filterExpenses(it) }
                .map { sortExpenses(it) }
                .map { createSummarySection(it) + createExpenseSection(it) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getExpenses(): Flowable<List<Expense>> {
        return database.expenseDao().getAll()
    }

    private fun filterExpenses(expenses: List<Expense>): List<Expense> {
        return expenses.filter { dateRange.contains(it.date) }
    }

    private fun sortExpenses(expenses: List<Expense>): List<Expense> {
        var comparator = compareByDescending<Expense> { it.date.time }
        comparator = comparator.thenBy { it.title }
        return expenses.sortedWith(comparator)
    }

    private fun createSummarySection(expenses: List<Expense>): List<HomeItemModel> {
        val context = getApplication<Application>()
        val currencySummaries = createCurrencySummaries(expenses)
        val summaryItemModel = SummaryItemModel(context, currencySummaries, dateRange)
        summaryItemModel.dateRangeChange = { dateRange ->
            this.dateRange = dateRange
            unsubscribeItemModels()
            subscribeItemModels()
        }
        return listOf(summaryItemModel)
    }

    private fun createCurrencySummaries(expenses: List<Expense>): List<Pair<Currency, Float>> {
        return expenses
                .groupBy({ it.currency }, { it.amount })
                .map { Pair(it.key, it.value.sum()) }
                .sortedByDescending { it.second }
    }

    private fun createExpenseSection(expenses: List<Expense>): List<HomeItemModel> {
        return expenses.map { expense -> createExpenseItemModel(expense) }
    }

    private fun createExpenseItemModel(expense: Expense): ExpenseItemModel {
        val context = getApplication<Application>()
        val itemModel = ExpenseItemModel(context, expense)
        itemModel.click = { showExpenseDetail.next(expense) }
        return itemModel
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeItemModels()
    }

    private fun unsubscribeItemModels() {
        itemModelsDisposable?.dispose()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeFragmentModel(application) as T
        }
    }
}

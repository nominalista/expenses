package com.nominalista.expenses.ui.home

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.infrastructure.utils.DataEvent
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeFragmentModel(
        application: Application,
        private val databaseDataSource: DatabaseDataSource
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<HomeItemModel>())
    val tags = Variable(emptyList<Tag>())
    val showExpenseDetail = DataEvent<Expense>()

    private var dateRange: DateRange = DateRange.AllTime
    private var tagFilter: TagFilter? = null
    private var itemModelsDisposable: Disposable? = null
    private var tagDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeItemModels()
        subscribeTags()
    }

    private fun subscribeItemModels() {
        itemModelsDisposable = getExpenses()
                .observeOn(Schedulers.io())
                .map { filterExpenses(it) }
                .map { sortExpenses(it) }
                .map { createSummarySection(it) + createExpenseSection(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { itemModels.value = it }
    }

    private fun getExpenses() = databaseDataSource.getExpenses()

    private fun filterExpenses(expenses: List<Expense>) = expenses
            .filter { dateRange.contains(it.date) }
            .filter { tagFilter?.containsAnyOf(it.tags) ?: true }

    private fun sortExpenses(expenses: List<Expense>): List<Expense> {
        return expenses.sortedByDescending { it.date.utcTimestamp }
    }

    private fun createSummarySection(expenses: List<Expense>): List<HomeItemModel> {
        val summarySection = ArrayList<HomeItemModel>()
        summarySection.add(createSummaryItemModel(expenses))
        val filter = tagFilter
        if (filter != null) summarySection.add(createTagFilterItemModel(filter))
        return summarySection
    }

    private fun createSummaryItemModel(expenses: List<Expense>): SummaryItemModel {
        val context = getApplication<Application>()
        val currencySummaries = createCurrencySummaries(expenses)
        val summaryItemModel = SummaryItemModel(context, currencySummaries, dateRange)
        summaryItemModel.dateRangeChange = {
            dateRange = it
            unsubscribeItemModels()
            subscribeItemModels()
        }
        return summaryItemModel
    }

    private fun createCurrencySummaries(expenses: List<Expense>): List<Pair<Currency, Float>> {
        return expenses
                .groupBy({ it.currency }, { it.amount })
                .map { Pair(it.key, it.value.sum()) }
                .sortedByDescending { it.second }
    }

    private fun createTagFilterItemModel(tagFilter: TagFilter): TagFilterItemModel {
        val itemModel = TagFilterItemModel(tagFilter)
        itemModel.clearClick = { clearTagFilter() }
        return itemModel
    }

    private fun createExpenseSection(expenses: List<Expense>): List<HomeItemModel> {
        return expenses.map { expense -> createExpenseItemModel(expense) }
    }

    private fun createExpenseItemModel(expense: Expense): ExpenseItemModel {
        val itemModel = ExpenseItemModel(expense)
        itemModel.click = { showExpenseDetail.next(expense) }
        return itemModel
    }

    private fun subscribeTags() {
        tagDisposable = getTags()
                .observeOn(Schedulers.io())
                .map { sortTags(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { tags.value = it }
    }

    private fun getTags() = databaseDataSource.getTags()

    private fun sortTags(tags: List<Tag>) = tags.sortedBy { it.name }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        unsubscribeItemModels()
        unsubscribeTags()
    }

    private fun unsubscribeItemModels() {
        itemModelsDisposable?.dispose()
    }

    private fun unsubscribeTags() {
        tagDisposable?.dispose()
    }

    // Action

    fun tagsFiltered(tagFilter: TagFilter) {
        this.tagFilter = tagFilter
        reloadItemModels()
    }

    // Common

    private fun clearTagFilter() {
        tagFilter = null
        reloadItemModels()
    }

    private fun reloadItemModels() {
        unsubscribeItemModels()
        subscribeItemModels()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            return HomeFragmentModel(application, databaseDataSource) as T
        }
    }
}

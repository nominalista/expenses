package com.nominalista.expenses.userinterface.home

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.automaton.ApplicationAutomaton
import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailInput.SetExpenseInput
import com.nominalista.expenses.automaton.home.HomeInput.*
import com.nominalista.expenses.automaton.home.HomeState
import com.nominalista.expenses.automaton.home.HomeState.ExpenseState
import com.nominalista.expenses.automaton.home.HomeState.TagState
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeFragmentModel(
        application: Application,
        private val automaton: ApplicationAutomaton
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<HomeItemModel>())
    val isLoading = Variable(false)
    val showExpenseDetail = Event()
    val showTagFiltering = Event()
    val showNoAddedTags = Event()

    var tags = emptyList<Tag>()

    private var dateRange: DateRange = DateRange.AllTime
    private var tagFilter: TagFilter? = null

    private var automatonDisposable: Disposable? = null
    private var itemModelsUpdateDisposable: Disposable? = null
    private var tagsUpdateDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeAutomaton()
        sendLoadExpenses()
        sendLoadTags()
    }

    private fun subscribeAutomaton() {
        automatonDisposable = automaton.state
                .map { it.homeState }
                .distinctUntilChanged()
                .subscribe { stateChanged(it) }
    }

    private fun stateChanged(homeState: HomeState) {
        dateRange = homeState.dateRange
        tagFilter = homeState.tagFilter

        val expenses = (homeState.expenseState as? ExpenseState.Expenses)?.expenses ?: emptyList()
        updateItemModels(expenses)
        val tags = (homeState.tagState as? TagState.Tags)?.tags ?: emptyList()
        updateTags(tags)

        isLoading.value = homeState.expenseState == ExpenseState.Loading
    }

    private fun updateItemModels(expenses: List<Expense>) {
        itemModelsUpdateDisposable?.dispose()
        itemModelsUpdateDisposable = Observable.just(expenses)
                .observeOn(Schedulers.computation())
                .map { filterExpenses(it) }
                .map { sortExpenses(it) }
                .map { createSummarySection(it) + createExpenseSection(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    itemModels.value = it
                    itemModelsUpdateDisposable = null
                }
    }

    private fun filterExpenses(expenses: List<Expense>) =
            expenses.filter { filterByDate(it.date) }.filter { filterByTags(it.tags) }

    private fun filterByDate(date: Date) = dateRange.contains(date)

    private fun filterByTags(tags: List<Tag>) = tags.containsAll(tagFilter?.tags ?: emptyList())

    private fun sortExpenses(expenses: List<Expense>) =
            expenses.sortedByDescending { it.date.utcTimestamp }

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
        summaryItemModel.dateRangeChange = { sendSetDateRange(it) }
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
        itemModel.clearClick = { sendSetTagFilter(null) }
        return itemModel
    }

    private fun createExpenseSection(expenses: List<Expense>) =
            expenses.map { expense -> createExpenseItemModel(expense) }

    private fun createExpenseItemModel(expense: Expense): ExpenseItemModel {
        val itemModel = ExpenseItemModel(expense)
        itemModel.click = { sendSetExpense(expense); showExpenseDetail.next() }
        return itemModel
    }

    private fun updateTags(tags: List<Tag>) {
        tagsUpdateDisposable?.dispose()
        tagsUpdateDisposable = Observable.just(tags)
                .observeOn(Schedulers.computation())
                .map { sortTags(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.tags = it
                    this.tagsUpdateDisposable = null
                }
    }

    private fun sortTags(tags: List<Tag>) = tags.sortedBy { it.name }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        unsubscribeAutomaton()
        maybeDisposeItemModelsUpdate()
        sendRestoreState()
    }

    private fun unsubscribeAutomaton() {
        automatonDisposable?.dispose()
        automatonDisposable = null
    }

    private fun maybeDisposeItemModelsUpdate() {
        itemModelsUpdateDisposable?.dispose()
        itemModelsUpdateDisposable = null
    }

    // Public

    fun filterSelected() {
        if (tags.isEmpty()) showNoAddedTags.next()
        else showTagFiltering.next()
    }

    fun tagsFiltered(tagFilter: TagFilter) = sendSetTagFilter(tagFilter)

    // Sending inputs

    private fun sendLoadExpenses() = automaton.send(LoadExpensesInput)

    private fun sendLoadTags() = automaton.send(LoadTagsInput)

    private fun sendSetDateRange(dateRange: DateRange) =
            automaton.send(SetDateRangeInput(dateRange))

    private fun sendSetTagFilter(tagFilter: TagFilter?) =
            automaton.send(SetTagFilterInput(tagFilter))

    private fun sendSetExpense(expense: Expense) = automaton.send(SetExpenseInput(expense))

    private fun sendRestoreState() = automaton.send(RestoreStateInput)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val automaton = application.automaton
            return HomeFragmentModel(application, automaton) as T
        }
    }
}

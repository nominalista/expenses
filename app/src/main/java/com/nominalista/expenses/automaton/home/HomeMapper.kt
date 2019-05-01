package com.nominalista.expenses.automaton.home

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.automaton.home.HomeInput.*
import com.nominalista.expenses.automaton.home.HomeState.ExpenseState
import com.nominalista.expenses.automaton.home.HomeState.TagState
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

typealias HomeMapperResult = Pair<HomeState, ApplicationOutput?>

class HomeMapper(private val databaseDataSource: DatabaseDataSource) {

    fun map(state: HomeState, input: HomeInput): HomeMapperResult {
        return when (input) {
            is SetExpenseStateInput -> setExpenseState(state, input)
            is SetTagStateInput -> setTagState(state, input)
            is SetDateRangeInput -> setDateRange(state, input)
            is SetTagFilterInput -> setTagFilter(state, input)
            is LoadExpensesInput -> loadExpenses(state)
            is LoadTagsInput -> loadTags(state)
            is RestoreStateInput -> restoreState()
        }
    }

    private fun setExpenseState(state: HomeState, input: SetExpenseStateInput) =
            HomeMapperResult(state.copy(expenseState = input.expenseState), empty())

    private fun setTagState(state: HomeState, input: SetTagStateInput) =
            HomeMapperResult(state.copy(tagState = input.tagState), empty())

    private fun setDateRange(state: HomeState, input: SetDateRangeInput) =
            HomeMapperResult(state.copy(dateRange = input.dateRange), empty())

    private fun setTagFilter(state: HomeState, input: SetTagFilterInput) =
            HomeMapperResult(state.copy(tagFilter = input.tagFilter), empty())

    private fun loadExpenses(state: HomeState): HomeMapperResult {
        val output = loadExpensesFromDatabase()
                .map { SetExpenseStateInput(ExpenseState.Expenses(it)) as ApplicationInput }
                .startWith(SetExpenseStateInput(ExpenseState.Loading) as ApplicationInput)
        return HomeMapperResult(state, output)
    }

    private fun loadExpensesFromDatabase(): Observable<List<Expense>> {
        return databaseDataSource.getExpenses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun loadTags(state: HomeState): HomeMapperResult {
        val output = loadTagsFromDatabase()
                .map { SetTagStateInput(TagState.Tags(it)) as ApplicationInput }
                .startWith(SetTagStateInput(TagState.Loading) as ApplicationInput)
        return HomeMapperResult(state, output)
    }

    private fun loadTagsFromDatabase(): Observable<List<Tag>> {
        return databaseDataSource.observeTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun restoreState() = HomeMapperResult(HomeState.INITIAL, empty())
}
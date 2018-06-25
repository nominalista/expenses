package com.nominalista.expenses.automaton.home

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeInputs.*
import com.nominalista.expenses.automaton.ApplicationOutput
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

    fun map(state: HomeState, input: ApplicationInput): HomeMapperResult {
        return when (input) {
            is SetExpenseStateInput -> setExpenseState(state, input)
            is SetTagStateInput -> setTagState(state, input)
            is SetDateRangeInput -> setDateRange(state, input)
            is SetTagFilterInput -> setTagFilter(state, input)
            is LoadExpensesInput -> loadExpenses(state)
            is LoadTagsInput -> loadTags(state)
            is RestoreStateInput -> restoreState()
            else -> HomeMapperResult(state, null)
        }
    }

    private fun setExpenseState(state: HomeState, input: SetExpenseStateInput): HomeMapperResult {
        val newState = HomeState(input.expenseState,
                state.tagState,
                state.dateRange,
                state.tagFilter)
        return HomeMapperResult(newState, empty())
    }

    private fun setTagState(state: HomeState, input: SetTagStateInput): HomeMapperResult {
        val newState = HomeState(state.expenseState,
                input.tagState,
                state.dateRange,
                state.tagFilter)
        return HomeMapperResult(newState, empty())
    }

    private fun setDateRange(oldDate: HomeState, input: SetDateRangeInput): HomeMapperResult {
        val newState = HomeState(oldDate.expenseState,
                oldDate.tagState,
                input.dateRange,
                oldDate.tagFilter)
        return HomeMapperResult(newState, empty())
    }

    private fun setTagFilter(oldState: HomeState, input: SetTagFilterInput): HomeMapperResult {
        val newState = HomeState(oldState.expenseState,
                oldState.tagState,
                oldState.dateRange,
                input.tagFilter)
        return HomeMapperResult(newState, empty())
    }

    private fun loadExpenses(oldState: HomeState): HomeMapperResult {
        val output = loadExpensesFromDatabase()
                .map { SetExpenseStateInput(ExpenseState.Expenses(it)) as ApplicationInput }
                .startWith(SetExpenseStateInput(ExpenseState.Loading) as ApplicationInput)
        return HomeMapperResult(oldState, output)
    }

    private fun loadExpensesFromDatabase(): Observable<List<Expense>> {
        return databaseDataSource.getExpenses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun loadTags(oldState: HomeState): HomeMapperResult {
        val output = loadTagsFromDatabase()
                .map { SetTagStateInput(TagState.Tags(it)) as ApplicationInput }
                .startWith(SetTagStateInput(TagState.Loading) as ApplicationInput)
        return HomeMapperResult(oldState, output)
    }

    private fun loadTagsFromDatabase(): Observable<List<Tag>> {
        return databaseDataSource.getTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun restoreState() = HomeMapperResult(
            HomeState.INITIAL,
            empty())
}
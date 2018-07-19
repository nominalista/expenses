package com.nominalista.expenses.automaton.newexpense

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeInput
import com.nominalista.expenses.automaton.newexpense.NewExpenseInput.*
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionInput
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionMapper
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io

typealias NewExpenseMapperResult = Pair<NewExpenseState, ApplicationOutput?>

class NewExpenseMapper(
        private val databaseDataSource: DatabaseDataSource,
        private val preferenceDataSource: PreferenceDataSource
) {

    private val tagSelectionMapper = TagSelectionMapper(databaseDataSource)

    fun map(state: NewExpenseState, input: NewExpenseInput): NewExpenseMapperResult {
        return when (input) {
            is SetSelectedCurrencyInput -> setSelectedCurrency(state, input)
            is SetSelectedDateInput -> setSelectedDate(state, input)
            is SetSelectedTagsInput -> setSelectedTags(state, input)
            is SetAmountInput -> setAmount(state, input)
            is SetTitleInput -> setTitle(state, input)
            is SetNotesInput -> setNotes(state, input)
            is LoadDefaultCurrencyInput -> loadDefaultCurrency(state, input)
            is CreateExpenseInput -> createExpense(state, input)
            is RestoreStateInput -> restoreState()
            is TagSelectionInput -> map(state, input)
            else -> NewExpenseMapperResult(state, null)
        }
    }

    private fun map(state: NewExpenseState, input: TagSelectionInput): NewExpenseMapperResult {
        val tagSelectionState = state.tagSelectionState
        val (newTagSelectionState, output) = tagSelectionMapper.map(tagSelectionState, input)
        val newState = state.copy(tagSelectionState = newTagSelectionState)
        return NewExpenseMapperResult(newState, output)
    }

    private fun setSelectedCurrency(state: NewExpenseState, input: SetSelectedCurrencyInput) =
            NewExpenseMapperResult(state.copy(selectedCurrency = input.selectedCurrency), empty())

    private fun setSelectedDate(state: NewExpenseState, input: SetSelectedDateInput) =
            NewExpenseMapperResult(state.copy(selectedDate = input.selectedDate), empty())

    private fun setSelectedTags(state: NewExpenseState, input: SetSelectedTagsInput) =
            NewExpenseMapperResult(state.copy(selectedTags = input.selectedTags), empty())

    private fun setAmount(state: NewExpenseState, input: SetAmountInput) =
            NewExpenseMapperResult(state.copy(amount = input.amount), empty())

    private fun setTitle(state: NewExpenseState, input: SetTitleInput) =
            NewExpenseMapperResult(state.copy(title = input.title), empty())

    private fun setNotes(state: NewExpenseState, input: SetNotesInput) =
            NewExpenseMapperResult(state.copy(notes = input.notes), empty())

    private fun loadDefaultCurrency(
            state: NewExpenseState,
            input: LoadDefaultCurrencyInput
    ): NewExpenseMapperResult {
        val output = loadDefaultCurrencyFromPreferences(input.context)
                .map { SetSelectedCurrencyInput(it) as ApplicationInput }
        return NewExpenseMapperResult(state, output)
    }

    private fun loadDefaultCurrencyFromPreferences(context: Context): Observable<Currency> {
        return preferenceDataSource.getDefaultCurrency(context)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun createExpense(
            state: NewExpenseState,
            input: CreateExpenseInput
    ): NewExpenseMapperResult {
        val output = insertExpenseIntoDatabase(input.expense)
                .map { HomeInput.LoadExpensesInput as ApplicationInput }
        return NewExpenseMapperResult(state, output)
    }

    private fun insertExpenseIntoDatabase(expense: Expense): Observable<Long> {
        return databaseDataSource.insertExpense(expense)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun restoreState() = NewExpenseMapperResult(NewExpenseState.INITIAL, empty())
}
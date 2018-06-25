package com.nominalista.expenses.automaton.newexpense

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeInputs
import com.nominalista.expenses.automaton.newexpense.NewExpenseInputs.*
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionMapper
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.infrastructure.automaton.maybeCombineOutputs
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io

typealias NewExpenseMapperResult = Pair<NewExpenseState, ApplicationOutput?>

class NewExpenseMapper(
        private val databaseDataSource: DatabaseDataSource,
        private val preferenceDataSource: PreferenceDataSource
) {

    private val tagSelectionMapper = TagSelectionMapper(
            databaseDataSource)

    fun map(state: NewExpenseState, input: ApplicationInput): NewExpenseMapperResult {
        val (internalState, internalOutput) = internalMap(state, input)
        val (tagSelectionState, tagSelectionOutput) =
                tagSelectionMapper.map(state.tagSelectionState, input)
        val newState = NewExpenseState(internalState.selectedCurrency,
                internalState.selectedDate,
                internalState.selectedTags,
                internalState.amount,
                internalState.title,
                internalState.notes,
                tagSelectionState)
        val output = maybeCombineOutputs(internalOutput, tagSelectionOutput)
        return NewExpenseMapperResult(newState, output)
    }

    private fun internalMap(
            state: NewExpenseState,
            input: ApplicationInput
    ): NewExpenseMapperResult {
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
            else -> NewExpenseMapperResult(state, null)
        }
    }

    private fun setSelectedCurrency(
            oldState: NewExpenseState,
            input: SetSelectedCurrencyInput
    ): NewExpenseMapperResult {
        val newState = NewExpenseState(input.selectedCurrency,
                oldState.selectedDate,
                oldState.selectedTags,
                oldState.amount,
                oldState.title,
                oldState.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun setSelectedDate(
            oldState: NewExpenseState,
            input: SetSelectedDateInput
    ): NewExpenseMapperResult {
        val newState = NewExpenseState(oldState.selectedCurrency,
                input.selectedDate,
                oldState.selectedTags,
                oldState.amount,
                oldState.title,
                oldState.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun setSelectedTags(
            oldState: NewExpenseState,
            input: SetSelectedTagsInput
    ): NewExpenseMapperResult {
        val newState = NewExpenseState(oldState.selectedCurrency,
                oldState.selectedDate,
                input.selectedTags,
                oldState.amount,
                oldState.title,
                oldState.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun setAmount(
            oldState: NewExpenseState,
            input: SetAmountInput
    ): NewExpenseMapperResult {
        val newState = NewExpenseState(oldState.selectedCurrency,
                oldState.selectedDate,
                oldState.selectedTags,
                input.amount,
                oldState.title,
                oldState.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun setTitle(oldState: NewExpenseState, input: SetTitleInput): NewExpenseMapperResult {
        val newState = NewExpenseState(oldState.selectedCurrency,
                oldState.selectedDate,
                oldState.selectedTags,
                oldState.amount,
                input.title,
                oldState.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun setNotes(oldState: NewExpenseState, input: SetNotesInput): NewExpenseMapperResult {
        val newState = NewExpenseState(oldState.selectedCurrency,
                oldState.selectedDate,
                oldState.selectedTags,
                oldState.amount,
                oldState.title,
                input.notes,
                oldState.tagSelectionState)
        return NewExpenseMapperResult(newState, empty())
    }

    private fun loadDefaultCurrency(
            oldState: NewExpenseState,
            input: LoadDefaultCurrencyInput
    ): NewExpenseMapperResult {
        val output: ApplicationOutput = loadDefaultCurrencyFromPreferences(input.context)
                .map { SetSelectedCurrencyInput(it) }
        return NewExpenseMapperResult(oldState, output)
    }

    private fun loadDefaultCurrencyFromPreferences(context: Context): Observable<Currency> {
        return preferenceDataSource.getDefaultCurrency(context)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun createExpense(
            oldState: NewExpenseState,
            input: CreateExpenseInput
    ): NewExpenseMapperResult {
        val output = insertExpenseIntoDatabase(input.expense)
                .map { HomeInputs.LoadExpensesInput as ApplicationInput }
        return NewExpenseMapperResult(oldState, output)
    }

    private fun insertExpenseIntoDatabase(expense: Expense): Observable<Long> {
        return databaseDataSource.insertExpense(expense)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun restoreState() =
            NewExpenseMapperResult(NewExpenseState.INITIAL, empty())
}
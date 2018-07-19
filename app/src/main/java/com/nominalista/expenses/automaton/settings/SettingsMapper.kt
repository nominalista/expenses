package com.nominalista.expenses.automaton.settings

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.automaton.home.HomeInput
import com.nominalista.expenses.automaton.settings.SettingsInput.*
import com.nominalista.expenses.automaton.settings.SettingsState.ExpenseExportState
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.task.ExportExpensesTask
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io

typealias SettingsMapperResult = Pair<SettingsState, ApplicationOutput?>

class SettingsMapper(
        private val databaseDataSource: DatabaseDataSource,
        private val preferenceDataSource: PreferenceDataSource
) {

    fun map(state: SettingsState, input: SettingsInput): SettingsMapperResult {
        return when (input) {
            is SetDefaultCurrencyInput -> setDefaultCurrency(state, input)
            is SetExpenseExportStateInput -> setExpenseExportState(state, input)
            is LoadDefaultCurrencyInput -> loadDefaultCurrency(state, input)
            is SaveDefaultCurrencyInput -> saveDefaultCurrency(state, input)
            is ExportExpensesInput -> exportExpenses(state, input)
            is DeleteAllExpensesInput -> deleteAllExpenses(state)
            is RestoreStateInput -> restoreState()
        }
    }

    private fun setDefaultCurrency(state: SettingsState, input: SetDefaultCurrencyInput) =
            SettingsMapperResult(state.copy(defaultCurrency = input.defaultCurrency), empty())

    private fun setExpenseExportState(
            state: SettingsState,
            input: SetExpenseExportStateInput
    ): SettingsMapperResult {
        val expenseExportState = input.expenseExportState
        val output = when (expenseExportState) {
            is ExpenseExportState.None, is ExpenseExportState.Working -> empty()
            is ExpenseExportState.Finished -> restoreExportExpenseState()
        }
        val newState = state.copy(expenseExportState = expenseExportState)
        return SettingsMapperResult(newState, output)
    }

    private fun restoreExportExpenseState(): ApplicationOutput =
            Observable.just(SetExpenseExportStateInput(ExpenseExportState.INITIAL))

    private fun loadDefaultCurrency(
            state: SettingsState,
            input: LoadDefaultCurrencyInput
    ): SettingsMapperResult {
        val output = loadDefaultCurrencyFromPreferences(input.context)
                .map { SetDefaultCurrencyInput(it) as ApplicationInput }
        return SettingsMapperResult(state, output)
    }

    private fun loadDefaultCurrencyFromPreferences(context: Context): Observable<Currency> {
        return preferenceDataSource.getDefaultCurrency(context)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun saveDefaultCurrency(
            state: SettingsState,
            input: SaveDefaultCurrencyInput
    ): SettingsMapperResult {
        val context = input.context
        val defaultCurrency = input.defaultCurrency
        val output = setDefaultCurrencyInPreferences(context, defaultCurrency)
                .andThen(Observable.just<ApplicationInput>(LoadDefaultCurrencyInput(input.context)))
        return SettingsMapperResult(state, output)
    }

    private fun setDefaultCurrencyInPreferences(
            context: Context,
            defaultCurrency: Currency
    ): Completable {
        return preferenceDataSource.setDefaultCurrency(context, defaultCurrency)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun exportExpenses(state: SettingsState, input: ExportExpensesInput) =
            SettingsMapperResult(state, executeExpenseExportTask(input.context, databaseDataSource))

    private fun executeExpenseExportTask(
            context: Context,
            databaseDataSource: DatabaseDataSource
    ): ApplicationOutput {
        val observable = Observable.create<ApplicationInput> { emitter ->
            val working = SetExpenseExportStateInput(ExpenseExportState.Working)
            emitter.onNext(working)

            val task = ExportExpensesTask(context, databaseDataSource)
            task.callback = { isSuccessful ->
                val state = ExpenseExportState.Finished(isSuccessful)
                emitter.onNext(SetExpenseExportStateInput(state))
                emitter.onComplete()
            }

            if (!emitter.isDisposed) task.execute()
        }

        return observable
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun deleteAllExpenses(state: SettingsState): SettingsMapperResult {
        val output = deleteAllExpensesFromDatabase()
                .andThen(Observable.just(HomeInput.LoadExpensesInput as ApplicationInput))
        return SettingsMapperResult(state, output)
    }

    private fun deleteAllExpensesFromDatabase(): Completable {
        return databaseDataSource.deleteAllExpenses()
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun restoreState() = SettingsMapperResult(SettingsState.INITIAL, empty())
}
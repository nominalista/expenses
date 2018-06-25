package com.nominalista.expenses.automaton.settings

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.automaton.home.HomeInputs
import com.nominalista.expenses.automaton.settings.SettingsInputs.*
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

    fun map(state: SettingsState, input: ApplicationInput): SettingsMapperResult {
        return when (input) {
            is SetDefaultCurrencyInput -> setDefaultCurrency(state, input)
            is SetExpenseExportStateInput -> setExpenseExportState(state, input)
            is LoadDefaultCurrencyInput -> loadDefaultCurrency(state, input)
            is SaveDefaultCurrencyInput -> saveDefaultCurrency(state, input)
            is ExportExpensesInput -> exportExpenses(state, input)
            is DeleteAllExpensesInput -> deleteAllExpenses(state)
            is RestoreStateInput -> restoreState()
            else -> SettingsMapperResult(state, null)
        }
    }

    private fun setDefaultCurrency(
            oldState: SettingsState,
            input: SetDefaultCurrencyInput
    ): SettingsMapperResult {
        val newState = SettingsState(input.defaultCurrency, oldState.expenseExportState)
        return SettingsMapperResult(newState, empty())
    }

    private fun setExpenseExportState(
            oldState: SettingsState,
            input: SetExpenseExportStateInput
    ): SettingsMapperResult {
        val exportExpensesState = input.expenseExportState
        val output = when (exportExpensesState) {
            is ExpenseExportState.None, is ExpenseExportState.Working -> empty()
            is ExpenseExportState.Finished -> restoreExportExpenseState()
        }
        val newState = SettingsState(oldState.defaultCurrency, exportExpensesState)
        return SettingsMapperResult(newState, output)
    }

    private fun restoreExportExpenseState(): ApplicationOutput =
            Observable.just(SetExpenseExportStateInput(ExpenseExportState.INITIAL))

    private fun loadDefaultCurrency(
            oldState: SettingsState,
            input: LoadDefaultCurrencyInput
    ): SettingsMapperResult {
        val output: ApplicationOutput = loadDefaultCurrencyFromPreferences(input.context)
                .map { SetDefaultCurrencyInput(it) }
        return SettingsMapperResult(oldState, output)
    }

    private fun loadDefaultCurrencyFromPreferences(context: Context): Observable<Currency> {
        return preferenceDataSource.getDefaultCurrency(context)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun saveDefaultCurrency(
            oldState: SettingsState,
            input: SaveDefaultCurrencyInput
    ): SettingsMapperResult {
        val context = input.context
        val defaultCurrency = input.defaultCurrency
        val output = setDefaultCurrencyInPreferences(context, defaultCurrency)
                .andThen(Observable.just<ApplicationInput>(LoadDefaultCurrencyInput(input.context)))
        return SettingsMapperResult(oldState, output)
    }

    private fun setDefaultCurrencyInPreferences(
            context: Context,
            defaultCurrency: Currency
    ): Completable {
        return preferenceDataSource.setDefaultCurrency(context, defaultCurrency)
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun exportExpenses(
            oldState: SettingsState,
            input: ExportExpensesInput
    ): SettingsMapperResult {
        val output = executeExpenseExportTask(input.context, databaseDataSource)
        val newState = SettingsState(oldState.defaultCurrency, oldState.expenseExportState)
        return SettingsMapperResult(newState, output)
    }

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

    private fun deleteAllExpenses(oldState: SettingsState): SettingsMapperResult {
        val output = deleteAllExpensesFromDatabase()
                .andThen(Observable.just(HomeInputs.LoadExpensesInput as ApplicationInput))
        return SettingsMapperResult(oldState, output)
    }

    private fun deleteAllExpensesFromDatabase(): Completable {
        return databaseDataSource.deleteAllExpenses()
                .subscribeOn(io())
                .observeOn(mainThread())
    }

    private fun restoreState() =
            SettingsMapperResult(SettingsState.INITIAL, empty())
}
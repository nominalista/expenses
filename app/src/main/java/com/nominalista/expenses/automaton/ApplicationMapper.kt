package com.nominalista.expenses.automaton

import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailMapper
import com.nominalista.expenses.automaton.home.HomeMapper
import com.nominalista.expenses.automaton.newexpense.NewExpenseMapper
import com.nominalista.expenses.automaton.settings.SettingsMapper
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.infrastructure.automaton.Mapper
import com.nominalista.expenses.infrastructure.automaton.maybeCombineOutputs
import io.reactivex.Observable

typealias ApplicationOutput = Observable<ApplicationInput>
typealias ApplicationMapperResult = Pair<ApplicationState, ApplicationOutput?>

class ApplicationMapper(
        databaseDataSource: DatabaseDataSource,
        preferenceDataSource: PreferenceDataSource
) : Mapper<ApplicationState, ApplicationInput> {

    private val expenseDetailMapper = ExpenseDetailMapper(
            databaseDataSource)
    private val homeMapper = HomeMapper(
            databaseDataSource)
    private val newExpenseMapper = NewExpenseMapper(
            databaseDataSource,
            preferenceDataSource)
    private val settingsMapper = SettingsMapper(
            databaseDataSource,
            preferenceDataSource)

    override fun map(state: ApplicationState, input: ApplicationInput): ApplicationMapperResult {
        val (expenseDetailState, expenseDetailOutput)
                = expenseDetailMapper.map(state.expenseDetailState, input)
        val (homeState, homeOutput) = homeMapper.map(state.homeState, input)
        val (newExpenseState, newExpenseOutput) = newExpenseMapper.map(state.newExpenseState, input)
        val (settingsState, settingsOutput) = settingsMapper.map(state.settingsState, input)

        val newState = ApplicationState(expenseDetailState,
                homeState,
                newExpenseState,
                settingsState)
        val output = maybeCombineOutputs(expenseDetailOutput,
                homeOutput,
                newExpenseOutput,
                settingsOutput)
        return ApplicationMapperResult(newState, output)
    }
}
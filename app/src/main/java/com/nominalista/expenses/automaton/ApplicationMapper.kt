package com.nominalista.expenses.automaton

import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailInput
import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailMapper
import com.nominalista.expenses.automaton.home.HomeInput
import com.nominalista.expenses.automaton.home.HomeMapper
import com.nominalista.expenses.automaton.settings.SettingsInput
import com.nominalista.expenses.automaton.settings.SettingsMapper
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.infrastructure.automaton.Mapper
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
    private val settingsMapper = SettingsMapper(
            databaseDataSource,
            preferenceDataSource)

    override fun map(state: ApplicationState, input: ApplicationInput): ApplicationMapperResult {
        return when (input) {
            is ExpenseDetailInput -> map(state, input)
            is HomeInput -> map(state, input)
            is SettingsInput -> map(state, input)
            else -> ApplicationMapperResult(state, null)
        }
    }

    private fun map(state: ApplicationState, input: ExpenseDetailInput): ApplicationMapperResult {
        val expenseDetailState = state.expenseDetailState
        val (newExpenseDetailState, output) = expenseDetailMapper.map(expenseDetailState, input)
        val newState = state.copy(expenseDetailState = newExpenseDetailState)
        return ApplicationMapperResult(newState, output)
    }

    private fun map(state: ApplicationState, input: HomeInput): ApplicationMapperResult {
        val homeState = state.homeState
        val (newHomeState, output) = homeMapper.map(homeState, input)
        val newState = state.copy(homeState = newHomeState)
        return ApplicationMapperResult(newState, output)
    }

    private fun map(state: ApplicationState, input: SettingsInput): ApplicationMapperResult {
        val settingsState = state.settingsState
        val (newSettingsState, output) = settingsMapper.map(settingsState, input)
        val newState = state.copy(settingsState = newSettingsState)
        return ApplicationMapperResult(newState, output)
    }
}
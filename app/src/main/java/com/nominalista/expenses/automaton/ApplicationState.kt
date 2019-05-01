package com.nominalista.expenses.automaton

import com.nominalista.expenses.automaton.expensedetail.ExpenseDetailState
import com.nominalista.expenses.automaton.home.HomeState
import com.nominalista.expenses.automaton.settings.SettingsState

data class ApplicationState(
    val expenseDetailState: ExpenseDetailState,
    val homeState: HomeState,
    val settingsState: SettingsState
) {
    companion object {
        val INITIAL = ApplicationState(
            ExpenseDetailState.INITIAL,
            HomeState.INITIAL,
            SettingsState.INITIAL
        )
    }
}
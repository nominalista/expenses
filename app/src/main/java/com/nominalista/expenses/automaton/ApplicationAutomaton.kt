package com.nominalista.expenses.automaton

import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.infrastructure.automaton.Automaton

class ApplicationAutomaton(
        databaseDataSource: DatabaseDataSource,
        preferenceDataSource: PreferenceDataSource
) : Automaton<ApplicationState, ApplicationInput>(ApplicationState.INITIAL,
        ApplicationMapper(databaseDataSource, preferenceDataSource))
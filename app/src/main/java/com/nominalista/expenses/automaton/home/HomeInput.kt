package com.nominalista.expenses.automaton.home

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeState.*
import com.nominalista.expenses.userinterface.home.DateRange
import com.nominalista.expenses.userinterface.home.TagFilter

sealed class HomeInput: ApplicationInput() {

    object LoadExpensesInput : HomeInput()

    object LoadTagsInput : HomeInput()

    data class SetDateRangeInput(val dateRange: DateRange) : HomeInput()

    data class SetExpenseStateInput(val expenseState: ExpenseState): HomeInput()

    data class SetTagFilterInput(val tagFilter: TagFilter?) : HomeInput()

    data class SetTagStateInput(val tagState: TagState): HomeInput()

    object RestoreStateInput : HomeInput()
}
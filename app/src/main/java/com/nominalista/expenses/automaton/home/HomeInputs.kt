package com.nominalista.expenses.automaton.home

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeState.*
import com.nominalista.expenses.userinterface.home.DateRange
import com.nominalista.expenses.userinterface.home.TagFilter

interface HomeInputs {

    object LoadExpensesInput : ApplicationInput

    object LoadTagsInput : ApplicationInput

    data class SetDateRangeInput(val dateRange: DateRange) : ApplicationInput

    data class SetExpenseStateInput(val expenseState: ExpenseState): ApplicationInput

    data class SetTagFilterInput(val tagFilter: TagFilter?) : ApplicationInput

    data class SetTagStateInput(val tagState: TagState): ApplicationInput

    object RestoreStateInput : ApplicationInput
}
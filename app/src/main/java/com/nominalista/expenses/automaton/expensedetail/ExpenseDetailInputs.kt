package com.nominalista.expenses.automaton.expensedetail

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.data.Expense

interface ExpenseDetailInputs {

    data class SetExpenseInput(val expense: Expense?) : ApplicationInput

    data class DeleteExpenseInput(val expense: Expense) : ApplicationInput

    object RestoreStateInput : ApplicationInput
}
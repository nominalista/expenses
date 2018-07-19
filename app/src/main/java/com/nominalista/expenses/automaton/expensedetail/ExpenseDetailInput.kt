package com.nominalista.expenses.automaton.expensedetail

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.data.Expense

sealed class ExpenseDetailInput: ApplicationInput() {

    data class SetExpenseInput(val expense: Expense?) : ExpenseDetailInput()

    data class DeleteExpenseInput(val expense: Expense) : ExpenseDetailInput()

    object RestoreStateInput : ExpenseDetailInput()
}
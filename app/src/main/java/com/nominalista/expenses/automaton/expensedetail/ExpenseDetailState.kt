package com.nominalista.expenses.automaton.expensedetail

import com.nominalista.expenses.data.Expense

data class ExpenseDetailState(val expense: Expense?) {
    companion object { val INITIAL = ExpenseDetailState(
            null)
    }
}
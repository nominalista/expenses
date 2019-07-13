package com.nominalista.expenses.home.domain

import com.nominalista.expenses.data.Expense

class SortExpensesUseCase {

    operator fun invoke(expenses: List<Expense>): List<Expense> {
        return expenses.sortedByDescending { it.date.utcTimestamp }
    }
}
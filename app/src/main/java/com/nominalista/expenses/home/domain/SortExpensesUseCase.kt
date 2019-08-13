package com.nominalista.expenses.home.domain

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.util.extensions.toEpochMillis

class SortExpensesUseCase {

    operator fun invoke(expenses: List<Expense>): List<Expense> {
        return expenses.sortedByDescending { it.date.toEpochMillis() }
    }
}
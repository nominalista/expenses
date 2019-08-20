package com.nominalista.expenses.home.domain

import com.nominalista.expenses.data.Expense

class SortExpensesUseCase {

    private val comparator by lazy {
        compareByDescending(Expense::date).thenByDescending(Expense::createdAt)
    }

    operator fun invoke(expenses: List<Expense>) = expenses.sortedWith(comparator)
}
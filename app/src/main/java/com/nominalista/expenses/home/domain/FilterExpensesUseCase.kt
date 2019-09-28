package com.nominalista.expenses.home.domain

import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.home.presentation.DateRange
import com.nominalista.expenses.home.presentation.TagFilter

class FilterExpensesUseCase {

    operator fun invoke(
        expenses: List<Expense>,
        dateRange: DateRange,
        tagFilter: TagFilter?
    ): List<Expense> {
        return expenses.filter { expense ->
            dateRange.contains(expense.date) &&
                    tagFilter?.let { expense.tags.containsAll(it.tags) } ?: true
        }
    }
}
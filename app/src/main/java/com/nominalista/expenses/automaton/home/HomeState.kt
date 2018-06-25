package com.nominalista.expenses.automaton.home

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.userinterface.home.DateRange
import com.nominalista.expenses.userinterface.home.TagFilter

data class HomeState(
        val expenseState: ExpenseState,
        val tagState: TagState,
        val dateRange: DateRange,
        val tagFilter: TagFilter?
) {

    sealed class ExpenseState {

        companion object {
            val INITIAL = None
        }

        object None : ExpenseState()
        object Loading : ExpenseState()
        data class Expenses(val expenses: List<Expense>) : ExpenseState()
    }

    sealed class TagState {

        companion object {
            val INITIAL = None
        }

        object None : TagState()
        object Loading : TagState()
        data class Tags(val tags: List<Tag>) : TagState()
    }

    companion object {
        val INITIAL = HomeState(ExpenseState.INITIAL,
                TagState.INITIAL,
                DateRange.AllTime,
                null)
    }
}
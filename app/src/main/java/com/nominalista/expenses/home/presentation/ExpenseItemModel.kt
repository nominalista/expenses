package com.nominalista.expenses.home.presentation

import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.util.extensions.toMoneyString
import com.nominalista.expenses.util.extensions.toString

class ExpenseItemModel(val expense: Expense) : HomeItemModel {

    val month = createMonth()
    val day = createDay()
    val amount = createAmount()
    val title = createTitle()

    var click: (() -> Unit)? = null

    private fun createMonth(): String {
        val pattern = "MMM"
        val monthName = expense.date.toString(pattern)
        return monthName.toUpperCase()
    }

    private fun createDay(): String {
        val day = expense.date.dayOfMonth
        return day.toString()
    }

    private fun createAmount(): String = expense.amount.toMoneyString(expense.currency)

    private fun createTitle() = expense.title
}
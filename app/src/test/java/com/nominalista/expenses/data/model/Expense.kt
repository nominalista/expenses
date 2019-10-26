package com.nominalista.expenses.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class ExpenseTest {

    @Test
    fun sortExpensesWithDifferentDates() {
        val expense1 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", null)
        val expense2 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 2), "", null)
        val expense3 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 3), "", null)

        val expensesToSort = listOf(expense3, expense2, expense1)
        val expensesInExpectedOrder = listOf(expense1, expense2, expense3)

        assertEquals(expensesToSort.sorted(), expensesInExpectedOrder)
    }

    @Test
    fun sortExpensesWithDifferentTimestamps() {
        val expense1 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", 0)
        val expense2 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", 1)
        val expense3 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", null)

        val expensesToSort = listOf(expense3, expense2, expense1)
        val expensesInExpectedOrder = listOf(expense1, expense2, expense3)

        assertEquals(expensesToSort.sorted(), expensesInExpectedOrder)
    }

    @Test
    fun sortExpensesWithDifferentDatesAndTimestamps() {
        val expense1 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", 0)
        val expense2 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 1), "", null)
        val expense3 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 2), "", 0)
        val expense4 =
            Expense("", 0.0, Currency.USD, "", emptyList(), LocalDate.of(2020, 1, 2), "", null)

        val expensesToSort = listOf(expense4, expense3, expense2, expense1)
        val expensesInExpectedOrder = listOf(expense1, expense2, expense3, expense4)

        assertEquals(expensesToSort.sorted(), expensesInExpectedOrder)
    }
}
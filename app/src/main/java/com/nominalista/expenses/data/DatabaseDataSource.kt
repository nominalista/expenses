package com.nominalista.expenses.data

import com.nominalista.expenses.infrastructure.utils.runOnBackground
import io.reactivex.Flowable

class DatabaseDataSource(private val database: ApplicationDatabase) {

    // Expenses

    fun getExpenses(): Flowable<List<Expense>> {
        return database.expenseDao().getAll()
                .map { it.map { expense -> addTagsToExpense(expense) } }
    }

    private fun addTagsToExpense(expense: Expense): Expense {
        expense.tags = getExpenseTags(expense)
        return expense
    }

    private fun getExpenseTags(expense: Expense): List<Tag> {
        return database.expenseTagJoinDao().getTagsWithExpenseId(expense.id)
    }

    fun insertExpense(expense: Expense) {
        runOnBackground {
            insertExpenseTagJoins(expense)
            database.expenseDao().insert(expense)
        }
    }

    private fun insertExpenseTagJoins(expense: Expense) {
        for (tag in expense.tags) {
            val join = ExpenseTagJoin(expense.id, tag.id)
            database.expenseTagJoinDao().insert(join)
        }
    }

    fun deleteExpense(expense: Expense) {
        runOnBackground {
            database.expenseDao().delete(expense)
        }
    }

    // Tags

    fun getTags(): Flowable<List<Tag>> {
        return database.tagDao().getAll()
    }

    fun insertTag(tag: Tag) {
        runOnBackground { database.tagDao().insert(tag) }
    }

    fun deleteTag(tag: Tag) {
        runOnBackground { database.tagDao().delete(tag) }
    }
}
package com.nominalista.expenses.data.database

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.ExpenseTagJoin
import com.nominalista.expenses.data.Tag
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
            val id = database.expenseDao().insert(expense)
            insertExpenseTagJoins(id, expense.tags)
        }
    }

    private fun insertExpenseTagJoins(expenseId: Long, tags: List<Tag>) {
        for (tag in tags) {
            val join = ExpenseTagJoin(expenseId, tag.id)
            database.expenseTagJoinDao().insert(join)
        }
    }

    fun deleteExpense(expense: Expense) {
        runOnBackground {
            database.expenseDao().delete(expense)
        }
    }

    fun deleteAllExpenses() {
        runOnBackground { database.expenseDao().deleteAll() }
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
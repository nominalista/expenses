package com.nominalista.expenses.data.local

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import io.reactivex.Flowable

class DatabaseDataSource(private val database: ApplicationDatabase) {

    // Expense

    fun getExpenses(): Flowable<List<Expense>> {
        return getExpenseEntities()
                .map { it.map { Pair(it, getTagEntitiesWithExpenseEntityId(it.id)) } }
                .map { mapToExpenses(it) }
    }

    private fun getExpenseEntities(): Flowable<List<com.nominalista.expenses.local.Expense>> {
        return database.expenseDao().getAll()
    }

    private fun getTagEntitiesWithExpenseEntityId(expenseEntityId: Int): List<com.nominalista.expenses.local.Tag> {
        return database.expenseTagDao().getTagsWithExpenseId(expenseEntityId)
    }

    // Tag

    fun getTags(): Flowable<List<Tag>> {
        return getTagEntities().map { mapToTags(it) }
    }

    private fun getTagEntities(): Flowable<List<com.nominalista.expenses.local.Tag>> {
        return database.tagDao().getAll()
    }

    // Common

    private fun mapToExpenses(pairs: List<Pair<com.nominalista.expenses.local.Expense, List<com.nominalista.expenses.local.Tag>>>): List<Expense> {
        return pairs.map { createExpense(it.first, mapToTags(it.second)) }
    }

    private fun createExpense(entity: com.nominalista.expenses.local.Expense, tags: List<Tag>): Expense {
        return Expense(entity.id,
                entity.amount,
                entity.currency,
                entity.title,
                entity.date,
                entity.notes,
                tags)
    }

    private fun mapToTags(entities: List<com.nominalista.expenses.local.Tag>): List<Tag> {
        return entities.map { createTag(it) }
    }

    private fun createTag(entity: com.nominalista.expenses.local.Tag): Tag {
        return Tag(entity.id, entity.name)
    }
}
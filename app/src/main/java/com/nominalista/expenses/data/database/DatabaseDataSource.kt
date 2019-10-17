package com.nominalista.expenses.data.database

import android.content.Context
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.old.Expense
import com.nominalista.expenses.data.model.old.ExpenseTagJoin
import com.nominalista.expenses.data.model.old.Tag
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class DatabaseDataSource(private val database: ApplicationDatabase) {

    // Expenses

    fun observeExpenses(): Observable<List<Expense>> {
        return database.expenseDao()
            .observeAll()
            .map { expenses -> expenses.map { addTagsToExpense(it) } }
    }

    fun getExpenses(): Single<List<Expense>> {
        return database.expenseDao()
            .getAll()
            .map { expenses -> expenses.map { addTagsToExpense(it) } }
    }

    fun observeExpense(expenseId: Long): Observable<Expense> {
        val expenseObservable = database.expenseDao().observeById(expenseId)
        val tagsObservable = database.expenseTagJoinDao().observeTagsByExpenseId(expenseId)
        return Observable.combineLatest(
            expenseObservable,
            tagsObservable,
            BiFunction { expense, tags -> expense.also { it.tags = tags } })
    }

    private fun addTagsToExpense(expense: Expense): Expense {
        expense.tags = getExpenseTags(expense)
        return expense
    }

    private fun getExpenseTags(expense: Expense): List<Tag> {
        return database.expenseTagJoinDao().getTagsWithExpenseId(expense.id)
    }

    fun insertExpense(expense: Expense): Observable<Long> {
        return Observable.defer {
            val id = database.expenseDao().insert(expense)

            for (tag in expense.tags) {
                val join = ExpenseTagJoin(id, tag.id)
                database.expenseTagJoinDao().insert(join)
            }

            Observable.just(id)
        }
    }

    fun updateExpense(expense: Expense): Completable {
        return Completable.fromAction {
            database.expenseTagJoinDao().deleteByExpenseId(expense.id)

            database.expenseDao().update(expense)

            for (tag in expense.tags) {
                val join =
                    ExpenseTagJoin(expense.id, tag.id)
                database.expenseTagJoinDao().insert(join)
            }
        }
    }

    fun deleteExpense(expense: Expense): Completable {
        return database.expenseDao().delete(expense)
    }

    fun deleteAllExpenses(): Completable {
        return Completable.defer { database.expenseDao().deleteAll(); Completable.complete() }
    }

    // Tags

    fun observeTags(): Observable<List<Tag>> {
        return database.tagDao().observeAll()
    }

    fun getTags(): Single<List<Tag>> {
        return Single.fromCallable { database.tagDao().getAll() }
    }

    /**
     * Insert tag to database provided that it contains unique name. Otherwise, return ID of the
     * first tag with same name.
     *
     * This method, unfortunately, doesn't pass information whether tag was inserted or not.
     * In further versions of database, tag ID should be replaced with just the name.
     */
    fun insertTagOrReturnIdOfTagWithSameName(tag: Tag): Single<Long> {
        return Single.fromCallable {
            val tagsWithSameName = database.tagDao().getByName(tag.name)

            if (tagsWithSameName.isEmpty()) {
                database.tagDao().insert(tag)
            } else {
                tagsWithSameName.first().id
            }
        }
    }

    fun deleteTag(tag: Tag): Completable {
        return Completable.fromAction { database.tagDao().delete(tag) }
    }

    fun deleteAllTags(): Completable {
        return Completable.fromAction { database.tagDao().deleteAll() }
    }

    companion object {

        fun getInstance(application: Application): DatabaseDataSource {
            return DatabaseDataSource(application.database)
        }
    }
}
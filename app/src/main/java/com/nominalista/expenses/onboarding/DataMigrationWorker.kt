package com.nominalista.expenses.onboarding

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.firebase.FirestoreDataSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers.io
import java.util.*
import com.nominalista.expenses.data.model.Expense as NewExpense
import com.nominalista.expenses.data.model.Tag as NewTag
import com.nominalista.expenses.data.model.old.Expense as OldExpense
import com.nominalista.expenses.data.model.old.Tag as OldTag

class DataMigrationWorker(context: Context, params: WorkerParameters) : RxWorker(context, params) {

    private val databaseDataSource: DatabaseDataSource by lazy {
        DatabaseDataSource.getInstance(applicationContext as Application)
    }

    private val firestoreDataSource: FirestoreDataSource by lazy {
        FirestoreDataSource.getInstance(applicationContext as Application)
    }

    override fun createWork(): Single<Result> {
        return migrateTags().andThen(migrateExpenses())
            .toSingleDefault(Result.success())
            .doOnSuccess { Log.d(TAG, "Succeeded to migrate data.") }
            .doOnError { Log.d(TAG, "Failed to migrate data: (${it.localizedMessage}).") }
    }

    private fun migrateTags(): Completable {
        return loadOldTags()
            .flatMapCompletable { insertNewTags(it) }
            .andThen(deleteOldTags())
    }

    private fun loadOldTags(): Single<List<OldTag>> {
        return databaseDataSource.getTags().subscribeOn(io())
    }

    private fun insertNewTags(oldTags: List<OldTag>): Completable {
        val tagNames = oldTags.map { it.name }.toSet()
        val newTags = tagNames.map { NewTag("", it) }
        val newTagsInsertions = newTags.map { firestoreDataSource.insertTag(it) }
        return Completable.merge(newTagsInsertions)
    }

    private fun deleteOldTags(): Completable {
        return databaseDataSource.deleteAllTags().subscribeOn(io())
    }

    private fun migrateExpenses(): Completable {
        return Single.zip(
            loadOldExpenses(),
            loadNewTags(),
            BiFunction { expenses: List<OldExpense>, tags: List<NewTag> -> expenses to tags }
        )
            .flatMapCompletable { insertNewExpenses(it.first, it.second) }
            .andThen(deleteOldExpenses())
    }

    private fun loadOldExpenses(): Single<List<OldExpense>> {
        val comparator = compareBy(OldExpense::date).thenBy(OldExpense::createdAt)
        return databaseDataSource.getExpenses().map { it.sortedWith(comparator) }
    }

    private fun loadNewTags(): Single<List<NewTag>> {
        return firestoreDataSource.getTags().subscribeOn(io())
    }

    private fun insertNewExpenses(
        oldExpenses: List<OldExpense>,
        newTags: List<NewTag>
    ): Completable {
        val newExpenses = oldExpenses.map { oldExpense ->
            val newExpenseTags = oldExpense.tags.mapNotNull { oldTag ->
                // Looks for the first new tag with the same name.
                newTags.firstOrNull { oldTag.name == it.name }
            }

            NewExpense(
                "",
                oldExpense.amount,
                oldExpense.currency,
                oldExpense.title,
                newExpenseTags,
                oldExpense.date,
                oldExpense.notes,
                null
            )
        }
        val newExpensesInsertions = newExpenses.map { firestoreDataSource.insertExpense(it) }
        return Completable.merge(newExpensesInsertions)
    }

    private fun deleteOldExpenses(): Completable {
        return databaseDataSource.deleteAllExpenses().subscribeOn(io())
    }

    companion object {

        private const val TAG = "DataMigrationWorker"

        fun enqueue(context: Context): UUID {
            val request = OneTimeWorkRequest.Builder(DataMigrationWorker::class.java).build()

            WorkManager.getInstance(context).enqueue(request)

            return request.id
        }
    }
}
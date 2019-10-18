package com.nominalista.expenses.onboarding

import android.content.Context
import android.util.Log
import androidx.work.*
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.firebase.FirestoreDataSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.coroutineScope
import java.util.*
import com.nominalista.expenses.data.model.Expense as NewExpense
import com.nominalista.expenses.data.model.Tag as NewTag
import com.nominalista.expenses.data.model.old.Expense as OldExpense
import com.nominalista.expenses.data.model.old.Tag as OldTag

class DataMigrationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val databaseDataSource: DatabaseDataSource by lazy {
        DatabaseDataSource.getInstance(applicationContext as Application)
    }

    private val firestoreDataSource: FirestoreDataSource by lazy {
        FirestoreDataSource.getInstance(applicationContext as Application)
    }

    override suspend fun doWork() = coroutineScope {
        val oldTags = getOldTags()
        insertNewTags(oldTags)

        val oldExpenses = getOldExpenses()
        val newTags = getNewTags()
        insertNewExpenses(oldExpenses, newTags)

        deleteOldTags()
        deleteOldExpenses()

        Log.d(TAG, "Succeeded to migrate data.")
        Result.success()
    }

    private fun getOldTags(): List<OldTag> {
        return databaseDataSource.getTags()
            .subscribeOn(io())
            .blockingGet()
    }

    private fun insertNewTags(oldTags: List<OldTag>) {
        val tagNames = oldTags.map { it.name }.toSet()
        val newTags = tagNames.map { NewTag("", it) }
        val newTagsInsertions = newTags.map { firestoreDataSource.insertTag(it) }
        Completable.merge(newTagsInsertions).blockingAwait()
    }

    private fun getOldExpenses(): List<OldExpense> {
        val comparator = compareBy(OldExpense::date).thenBy(OldExpense::createdAt)
        return databaseDataSource.getExpenses()
            .map { it.sortedWith(comparator) }
            .subscribeOn(io())
            .blockingGet()
    }

    private fun getNewTags(): List<NewTag> {
        return firestoreDataSource.getTags()
            .subscribeOn(io())
            .blockingGet()
    }

    private fun insertNewExpenses(oldExpenses: List<OldExpense>, newTags: List<NewTag>) {
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
        Completable.merge(newExpensesInsertions).blockingAwait()
    }

    private fun deleteOldTags() {
        databaseDataSource.deleteAllTags().subscribeOn(io()).blockingAwait()
    }

    private fun deleteOldExpenses() {
        databaseDataSource.deleteAllExpenses().subscribeOn(io()).blockingAwait()
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
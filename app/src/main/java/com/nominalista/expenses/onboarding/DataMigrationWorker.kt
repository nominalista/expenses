package com.nominalista.expenses.onboarding

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.data.firebase.FirebaseDataStore
import com.nominalista.expenses.data.room.RoomDataStore
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.coroutineScope
import java.util.*

class DataMigrationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val localDataStore: DataStore by lazy {
        (applicationContext as Application).localDataStore
    }

    private val cloudDataStore: DataStore by lazy {
        (applicationContext as Application).cloudDataStore
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

    private fun getOldTags(): List<Tag> {
        return localDataStore.getTags()
            .subscribeOn(io())
            .blockingGet()
    }

    private fun insertNewTags(oldTags: List<Tag>) {
        val tagNames = oldTags.map { it.name }.toSet()
        val newTags = tagNames.map { Tag("", it) }

        val newTagsInsertions = newTags.map {
            cloudDataStore.insertTag(it).ignoreElement()
        }

        Completable.merge(newTagsInsertions).blockingAwait()
    }

    private fun getOldExpenses(): List<Expense> {
        return localDataStore.getExpenses()
            .map { it.sorted() }
            .subscribeOn(io())
            .blockingGet()
    }

    private fun getNewTags(): List<Tag> {
        return cloudDataStore.getTags()
            .subscribeOn(io())
            .blockingGet()
    }

    private fun insertNewExpenses(oldExpenses: List<Expense>, newTags: List<Tag>) {
        val newExpenses = oldExpenses.map { oldExpense ->
            val newExpenseTags = oldExpense.tags.mapNotNull { oldTag ->
                // Looks for the first new tag with the same name.
                newTags.firstOrNull { oldTag.name == it.name }
            }

            Expense(
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

        val newExpensesInsertions = newExpenses.map {
            cloudDataStore.insertExpense(it).ignoreElement()
        }

        Completable.merge(newExpensesInsertions).blockingAwait()
    }

    private fun deleteOldTags() {
        localDataStore.deleteAllTags().subscribeOn(io()).blockingAwait()
    }

    private fun deleteOldExpenses() {
        localDataStore.deleteAllExpenses().subscribeOn(io()).blockingAwait()
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
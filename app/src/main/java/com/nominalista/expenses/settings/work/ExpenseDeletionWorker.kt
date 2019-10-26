package com.nominalista.expenses.settings.work

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.ListenableWorker.Result.success
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.firebase.FirestoreDataSource
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.util.extensions.application
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.coroutineScope
import java.util.*

class ExpenseDeletionWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val firestoreDataSource: FirestoreDataSource by lazy {
        FirestoreDataSource.getInstance(applicationContext as Application)
    }

    override suspend fun doWork() = coroutineScope {
        val expenses = getExpenses()
        deleteExpenses(expenses)

        Log.d(TAG, "Succeeded to delete all expenses.")
        success()
    }

    private fun getExpenses(): List<Expense> {
        return firestoreDataSource.getExpenses().blockingGet()
    }

    private fun deleteExpenses(expenses: List<Expense>) {
        expenses.forEach { firestoreDataSource.deleteExpense(it).blockingGet() }
    }

    companion object {

        private const val TAG = "ExpenseDeletionWorker"

        fun enqueue(context: Context): UUID {
            val request = OneTimeWorkRequest.Builder(ExpenseDeletionWorker::class.java).build()

            WorkManager.getInstance(context).enqueue(request)

            return request.id
        }
    }
}
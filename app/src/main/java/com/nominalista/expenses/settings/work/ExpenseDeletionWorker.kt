package com.nominalista.expenses.settings.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.success
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.data.store.DataStoreFactory
import kotlinx.coroutines.coroutineScope
import java.util.*

class ExpenseDeletionWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val dataStore: DataStore by lazy {
        DataStoreFactory.get(applicationContext as Application)
    }

    override suspend fun doWork() = coroutineScope {
        val expenses = getExpenses()
        deleteExpenses(expenses)

        Log.d(TAG, "Succeeded to delete all expenses.")
        success()
    }

    private fun getExpenses(): List<Expense> {
        return dataStore.getExpenses().blockingGet()
    }

    private fun deleteExpenses(expenses: List<Expense>) {
        expenses.forEach { dataStore.deleteExpense(it).blockingGet() }
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
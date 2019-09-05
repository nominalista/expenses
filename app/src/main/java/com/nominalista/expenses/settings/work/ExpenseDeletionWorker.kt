package com.nominalista.expenses.settings.work

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.util.extensions.application
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class ExpenseDeletionWorker(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return deleteExpenses().toSingleDefault(Result.success())
    }

    private fun deleteExpenses(): Completable {
        val database = applicationContext.application.database
        val databaseDataSource = DatabaseDataSource(database)
        return databaseDataSource.deleteAllExpenses()
    }

    companion object {

        fun enqueue(context: Context): UUID {
            val request = OneTimeWorkRequest.Builder(ExpenseDeletionWorker::class.java).build()

            WorkManager.getInstance(context).enqueue(request)

            return request.id
        }
    }
}
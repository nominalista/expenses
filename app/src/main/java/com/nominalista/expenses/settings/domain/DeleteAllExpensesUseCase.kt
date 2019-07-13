package com.nominalista.expenses.settings.domain

import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Completable

class DeleteAllExpensesUseCase(private val databaseDataSource: DatabaseDataSource) {

    operator fun invoke(): Completable {
        return databaseDataSource.deleteAllExpenses()
    }
}
package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Completable

class DeleteExpenseUseCase(private val databaseDataSource: DatabaseDataSource) {
    
    operator fun invoke(expense: Expense): Completable {
        return databaseDataSource.deleteExpense(expense)
    }
}
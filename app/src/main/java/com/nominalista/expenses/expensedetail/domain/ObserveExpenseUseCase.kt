package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Observable

class ObserveExpenseUseCase(private val databaseDataSource: DatabaseDataSource) {
    operator fun invoke(expenseId: Long): Observable<Expense> {
        return databaseDataSource.observeExpense(expenseId)
    }
}
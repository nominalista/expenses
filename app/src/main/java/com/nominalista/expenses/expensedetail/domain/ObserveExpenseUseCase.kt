package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.firebase.FirestoreDataSource
import com.nominalista.expenses.data.model.Expense
import io.reactivex.Observable

class ObserveExpenseUseCase(private val firestoreDataSource: FirestoreDataSource) {
    operator fun invoke(expenseId: String): Observable<Expense> {
        return firestoreDataSource.observeExpense(expenseId)
    }
}
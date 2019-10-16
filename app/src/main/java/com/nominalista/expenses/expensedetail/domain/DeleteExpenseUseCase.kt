package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.firebase.FirestoreDataSource
import com.nominalista.expenses.data.model.Expense
import io.reactivex.Completable

class DeleteExpenseUseCase(private val firestoreDataSource: FirestoreDataSource) {

    operator fun invoke(expense: Expense): Completable {
        return firestoreDataSource.deleteExpense(expense)
    }
}
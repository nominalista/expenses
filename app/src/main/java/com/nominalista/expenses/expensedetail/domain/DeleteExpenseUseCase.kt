package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.store.DataStore
import io.reactivex.Completable

class DeleteExpenseUseCase(private val dataStore: DataStore) {

    operator fun invoke(expense: Expense): Completable {
        return dataStore.deleteExpense(expense)
    }
}
package com.nominalista.expenses.expensedetail.domain

import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.store.DataStore
import io.reactivex.Observable

class ObserveExpenseUseCase(private val dataStore: DataStore) {
    operator fun invoke(expenseId: String): Observable<Expense> {
        return dataStore.observeExpense(expenseId)
    }
}
package com.nominalista.expenses.data.store

import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.data.model.Tag
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DataStore {

    // Expenses

    fun observeExpenses(): Observable<List<Expense>>

    fun getExpenses(): Single<List<Expense>>

    fun observeExpense(id: String): Observable<Expense>

    fun getExpense(id: String): Single<Expense>

    fun insertExpense(expense: Expense): Single<String>

    fun updateExpense(expense: Expense): Completable

    fun deleteExpense(expense: Expense): Completable

    fun deleteAllExpenses(): Completable

    // Tags

    fun observeTags(): Observable<List<Tag>>

    fun getTags(): Single<List<Tag>>

    fun insertTag(tag: Tag): Single<String>

    fun deleteTag(tag: Tag): Completable

    fun deleteAllTags(): Completable

    // Rules

    fun observeRules(): Observable<List<Rule>>

    fun getRules(): Single<List<Rule>>

    fun observeRule(id: String): Observable<Rule>

    fun getRule(id: String): Single<Rule>

    fun insertRule(rule: Rule): Single<String>

    fun updateRule(rule: Rule): Completable

    fun deleteRule(rule: Rule): Completable
}
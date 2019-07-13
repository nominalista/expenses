package com.nominalista.expenses.data.database.dao

import androidx.room.*
import com.nominalista.expenses.data.Expense
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ExpenseDao {

    @Query("SELECT * from expenses")
    fun observeAll(): Observable<List<Expense>>

    @Query("SELECT * FROM expenses")
    fun getAll(): Single<List<Expense>>

    @Query("SELECT * from expenses WHERE id = :id")
    fun observeById(id: Long): Observable<Expense>

    @Insert
    fun insert(expense: Expense): Long

    @Update
    fun update(expense: Expense)

    @Delete
    fun delete(expense: Expense): Completable

    @Query("DELETE FROM expenses")
    fun deleteAll()
}
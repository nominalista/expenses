package com.nominalista.expenses.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nominalista.expenses.data.room.entities.ExpenseEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ExpenseDao {

    @Query("SELECT * from expenses")
    fun observeAll(): Observable<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses")
    fun getAll(): Single<List<ExpenseEntity>>

    @Query("SELECT * from expenses WHERE id = :id")
    fun observeById(id: Long): Observable<ExpenseEntity>

    @Query("SELECT * from expenses WHERE id = :id")
    fun getById(id: Long): Single<ExpenseEntity>

    @Insert
    fun insert(expense: ExpenseEntity): Single<Long>

    @Update
    fun update(expense: ExpenseEntity): Completable

    @Query("DELETE from expenses WHERE id = :id")
    fun deleteById(id: Long): Completable

    @Query("DELETE FROM expenses")
    fun deleteAll(): Completable
}
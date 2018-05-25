package com.nominalista.expenses.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface ExpenseDao {

    @Query("SELECT * from expenses")
    fun getAll(): Flowable<List<Expense>>

    @Insert
    fun insert(expense: Expense)

    @Delete
    fun delete(expense: Expense)
}
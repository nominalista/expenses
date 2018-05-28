package com.nominalista.expenses.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nominalista.expenses.data.Expense
import io.reactivex.Flowable

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    fun getAll(): Flowable<List<Expense>>

    @Insert
    fun insert(expense: Expense): Long

    @Delete
    fun delete(expense: Expense)
}
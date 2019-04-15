package com.nominalista.expenses.data.database.dao

import androidx.room.*
import com.nominalista.expenses.data.Expense

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    fun getAll(): List<Expense>

    @Insert
    fun insert(expense: Expense): Long

    @Update
    fun update(expense: Expense)

    @Delete
    fun delete(expense: Expense)

    @Query("DELETE FROM expenses")
    fun deleteAll()
}
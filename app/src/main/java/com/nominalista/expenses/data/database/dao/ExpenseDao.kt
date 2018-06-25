package com.nominalista.expenses.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nominalista.expenses.data.Expense

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    fun getAll(): List<Expense>

    @Insert
    fun insert(expense: Expense): Long

    @Delete
    fun delete(expense: Expense)

    @Query("DELETE FROM expenses")
    fun deleteAll()
}
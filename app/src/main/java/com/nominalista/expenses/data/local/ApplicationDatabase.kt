package com.nominalista.expenses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.CurrencyConverter
import com.nominalista.expenses.infrastructure.utils.DateConverter

@Database(
        entities = [
            Expense::class,
            Tag::class,
            ExpenseTagEntity::class
        ],
        version = 1,
        exportSchema = false)
@TypeConverters(
        value = [
            CurrencyConverter::class,
            DateConverter::class
        ]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun tagDao(): TagDao

    abstract fun expenseTagDao(): ExpenseTagDao
}
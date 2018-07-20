package com.nominalista.expenses.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.ExpenseTagJoin
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.dao.ExpenseDao
import com.nominalista.expenses.data.dao.ExpenseTagJoinDao
import com.nominalista.expenses.data.dao.TagDao
import com.nominalista.expenses.infrastructure.utils.CurrencyConverter
import com.nominalista.expenses.infrastructure.utils.DateConverter

@Database(
        entities = [
            Expense::class,
            Tag::class,
            ExpenseTagJoin::class
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

    abstract fun expenseTagJoinDao(): ExpenseTagJoinDao
}
package com.nominalista.expenses.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.ExpenseTagJoin
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.converter.CurrencyConverter
import com.nominalista.expenses.data.database.converter.LocalDateConverter
import com.nominalista.expenses.data.database.dao.ExpenseDao
import com.nominalista.expenses.data.database.dao.ExpenseTagJoinDao
import com.nominalista.expenses.data.database.dao.TagDao

@Database(
    entities = [
        Expense::class,
        Tag::class,
        ExpenseTagJoin::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [
        CurrencyConverter::class,
        LocalDateConverter::class
    ]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun tagDao(): TagDao

    abstract fun expenseTagJoinDao(): ExpenseTagJoinDao
}
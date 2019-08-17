package com.nominalista.expenses.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

    companion object {

        fun build(context: Context) =
            Room.databaseBuilder(context, ApplicationDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()

        private const val DATABASE_NAME = "database"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN created_at INTEGER")
                database.execSQL("ALTER TABLE expenses ADD COLUMN modified_at INTEGER")
            }
        }
    }
}
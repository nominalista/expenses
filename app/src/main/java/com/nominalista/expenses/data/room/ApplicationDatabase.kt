package com.nominalista.expenses.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nominalista.expenses.data.room.converter.CurrencyConverter
import com.nominalista.expenses.data.room.converter.KeywordsConverter
import com.nominalista.expenses.data.room.converter.LocalDateConverter
import com.nominalista.expenses.data.room.dao.ExpenseDao
import com.nominalista.expenses.data.room.dao.ExpenseTagJoinDao
import com.nominalista.expenses.data.room.dao.RuleDao
import com.nominalista.expenses.data.room.dao.TagDao
import com.nominalista.expenses.data.room.entities.ExpenseEntity
import com.nominalista.expenses.data.room.entities.ExpenseTagJoinEntity
import com.nominalista.expenses.data.room.entities.RuleEntity
import com.nominalista.expenses.data.room.entities.TagEntity

@Database(
        entities = [
            ExpenseEntity::class,
            ExpenseTagJoinEntity::class,
            TagEntity::class,
            RuleEntity::class
        ],
        version = 3,
        exportSchema = true
)
@TypeConverters(
        value = [
            CurrencyConverter::class,
            LocalDateConverter::class,
            KeywordsConverter::class
        ]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun tagDao(): TagDao

    abstract fun expenseTagJoinDao(): ExpenseTagJoinDao

    abstract fun ruleDao(): RuleDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE expenses ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE expenses ADD COLUMN modified_at INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `rules` (`id` LONG, `name` TEXT, `firstSymbol` TEXT, `decimalSeparator` TEXT, `groupSeparator` TEXT," +
                        "PRIMARY KEY(`id`))")
            }
        }

        private const val DATABASE_NAME = "database"

        fun build(context: Context) =
                Room.databaseBuilder(context, ApplicationDatabase::class.java,
                        DATABASE_NAME
                )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .fallbackToDestructiveMigration()
                        .build()
    }
}
package com.nominalista.expenses

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.nominalista.expenses.data.room.ApplicationDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ApplicationDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val expenseId = 1L
        val expenseAmount = 100f
        val expenseCurrency = "USD"
        val expenseTitle = "Expense"
        val expenseDate = 1_000_000L
        val expenseNotes = "Notes"

        helper.createDatabase(TEST_DB_NAME, 1).apply {
            val values = ContentValues().apply {
                put("id", expenseId)
                put("amount", expenseAmount)
                put("currency", expenseCurrency)
                put("title", expenseTitle)
                put("date", expenseDate)
                put("notes", expenseNotes)
            }
            // Insert some data to database with initial version.
            insert("expenses", SQLiteDatabase.CONFLICT_REPLACE, values)
            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide MIGRATION_1_2 as the migration process.
        val database = helper.runMigrationsAndValidate(
            TEST_DB_NAME, 2, true, ApplicationDatabase.MIGRATION_1_2
        )

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        val cursor = database.query("SELECT * FROM expenses").apply { moveToFirst() }
        assertEquals(expenseId, cursor.getLong(cursor.getColumnIndex("id")))
        assertEquals(expenseAmount, cursor.getFloat(cursor.getColumnIndex("amount")))
        assertEquals(expenseCurrency, cursor.getString(cursor.getColumnIndex("currency")))
        assertEquals(expenseTitle, cursor.getString(cursor.getColumnIndex("title")))
        assertEquals(expenseDate, cursor.getLong(cursor.getColumnIndex("date")))
        assertEquals(expenseNotes, cursor.getString(cursor.getColumnIndex("notes")))
        assertEquals(0L, cursor.getLong(cursor.getColumnIndex("created_at")))
        assertEquals(0L, cursor.getLong(cursor.getColumnIndex("modified_at")))
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test.db"
    }
}
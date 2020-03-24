package com.nominalista.expenses.settings.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import androidx.work.ListenableWorker.Result.success
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.extensions.toLocalDate
import jxl.DateCell
import jxl.NumberCell
import jxl.Sheet
import jxl.Workbook
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.LocalDate
import java.util.*

class ExpenseImportWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val dataStore: DataStore by lazy {
        (applicationContext as Application).defaultDataStore
    }

    override suspend fun doWork() = coroutineScope {
        val fileUri = getFileUri()

        val workbook = getWorkbook(fileUri)
        val sheet = workbook.sheets.first()

        val tagNames = getTagNamesFromSheet(sheet)
        val tags = insertOrGetTagsForNames(tagNames)

        val expenses = getExpensesFromSheet(sheet, tags)
        insertExpenses(expenses)

        Log.d(TAG, "Succeeded to import expenses.")
        success()
    }

    private fun getFileUri(): Uri {
        val fileUriString = inputData.getString(KEY_FILE_URI) ?: error("Not found file URI.")
        return Uri.parse(fileUriString)
    }

    private fun getWorkbook(fileUri: Uri): Workbook {
        val fileInputStream = applicationContext.contentResolver.openInputStream(fileUri)
        return Workbook.getWorkbook(fileInputStream)
    }

    private fun getTagNamesFromSheet(sheet: Sheet): List<String> {
        val columnOfTags = getColumnIndexWithTitle(
            sheet,
            applicationContext.getString(R.string.column_tags)
        )

        // Skip the first row, cause it contains only titles.
        val startingRow = 1

        val tagNames = mutableSetOf<String>()

        for (row in startingRow until sheet.rows) {
            val cell = sheet.getCell(columnOfTags, row)
            cell.contents.split(", ").filter { it.isNotEmpty() }.forEach { tagNames.add(it) }
        }

        return tagNames.toList()
    }

    private fun insertOrGetTagsForNames(names: List<String>): List<Tag> {
        val existingTags = dataStore.getTags().blockingGet()

        val resultTags = mutableListOf<Tag>()

        names.forEach { name ->
            val existingTagWithSameName = existingTags.firstOrNull { it.name == name }
            if (existingTagWithSameName != null) {
                resultTags.add(existingTagWithSameName)
            } else {
                val id = dataStore.insertTag(Tag("", name)).blockingGet()
                resultTags.add(Tag(id, name))
            }
        }

        return resultTags
    }

    private fun getExpensesFromSheet(sheet: Sheet, allTags: List<Tag>): List<Expense> {
        val columnOfAmount =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_amount))
        val columnOfCurrency =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_currency))
        val columnOfTitle =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_title))
        val columnOfDate =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_date))
        val columnOfNotes =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_notes))
        val columnOfTags =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_tags))

        // Skip the first row, cause it contains only titles.
        val startingRow = 1

        val expenses = mutableListOf<Expense>()

        for (row in startingRow until sheet.rows) {
            val amount = getAmount(sheet, columnOfAmount, row)
            val currency = getCurrency(sheet, columnOfCurrency, row)
            val title = getTitle(sheet, columnOfTitle, row)
            val tags = getTags(sheet, columnOfTags, row, allTags)
            val date = getDate(sheet, columnOfDate, row)
            val notes = getNotes(sheet, columnOfNotes, row)

            expenses.add(Expense("", amount, currency, title, tags, date, notes, null))
        }

        return expenses
    }

    private fun getColumnIndexWithTitle(sheet: Sheet, title: String): Int {
        for (column in 0 until sheet.columns) {
            if (sheet.getCell(column, 0).contents == title) {
                return column
            }
        }

        return INVALID_COLUMN
    }

    private fun getAmount(sheet: Sheet, column: Int, row: Int): Double {
        val cell = sheet.getCell(column, row)

        return if (cell is NumberCell) {
            cell.value.toString().toDouble()
        } else {
            cell.contents.toDouble()
        }
    }

    private fun getCurrency(sheet: Sheet, column: Int, row: Int): Currency {
        val cell = sheet.getCell(column, row)
        return Currency.fromCode(cell.contents) ?: error("Invalid currency code.")
    }

    private fun getTitle(sheet: Sheet, column: Int, row: Int): String {
        return sheet.getCell(column, row).contents
    }

    private fun getTags(sheet: Sheet, column: Int, row: Int, allTags: List<Tag>): List<Tag> {
        return sheet.getCell(column, row)
            .contents
            .split(", ")
            .filter { it.isNotEmpty() }
            .map { name -> allTags.first { it.name == name } }
    }

    private fun getDate(sheet: Sheet, column: Int, row: Int): LocalDate {
        val cell = sheet.getCell(column, row)

        return if (cell is DateCell) {
            cell.date.toLocalDate()
        } else {
            cell.contents.toLocalDate(DATE_PATTERN)
        }
    }

    private fun getNotes(sheet: Sheet, column: Int, row: Int): String {
        return sheet.getCell(column, row).contents
    }

    private fun insertExpenses(expenses: List<Expense>) {
        expenses.forEach { dataStore.insertExpense(it).blockingGet() }
    }

    companion object {

        private const val TAG = "ExpenseImportWorker"

        private const val KEY_FILE_URI = "fileUri"

        private const val INVALID_COLUMN = -1

        private const val DATE_PATTERN = "yyyy-MM-dd"

        fun enqueue(context: Context, fileUri: Uri): UUID {
            val data = workDataOf(KEY_FILE_URI to fileUri.toString())

            val request = OneTimeWorkRequestBuilder<ExpenseImportWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(request)

            return request.id
        }
    }
}
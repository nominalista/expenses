package com.nominalista.expenses.settings.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.util.extensions.toLocalDate
import jxl.*
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.LocalDate
import java.util.*

class ExpenseImportWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val databaseDataSource by lazy {
        DatabaseDataSource((applicationContext as Application).database)
    }

    override suspend fun doWork() = coroutineScope {
        importExpenses(getFileUri())
        Result.success()
    }

    private fun getFileUri(): Uri {
        val fileUriString = inputData.getString(KEY_FILE_URI) ?: error("Not found file URI.")
        return Uri.parse(fileUriString)
    }

    private fun importExpenses(fileUri: Uri) {
        val workbook = getWorkbook(fileUri)
        val sheet = workbook.sheets.first()

        val allTags = getAndInsertAllTags(sheet)
        Log.d(TAG, "Succeeded to insert all ${allTags.size} tags.")

        val allExpenses = getAndInsertAllExpenses(sheet, allTags)
        Log.d(TAG, "Succeeded to insert all ${allExpenses.size} expenses.")
    }

    private fun getWorkbook(fileUri: Uri): Workbook {
        val fileInputStream = applicationContext.contentResolver.openInputStream(fileUri)
        return Workbook.getWorkbook(fileInputStream)
    }

    private fun getAndInsertAllTags(sheet: Sheet): Set<Tag> {
        val tagsForInsertions = hashSetOf<Tag>()

        val columnOfTags =
            getColumnIndexWithTitle(sheet, applicationContext.getString(R.string.column_tags))

        // Skip the first row, cause it contains only titles.
        val startingRow = 1

        for (row in startingRow until sheet.rows) {
            val cell = sheet.getCell(columnOfTags, row)
            cell.contents.split(", ")
                .filter { it.isNotEmpty() }
                .forEach { tagsForInsertions.add(Tag(0, it)) }
        }

        return tagsForInsertions.map { tagForInsertion ->
            val id = databaseDataSource.insertTagOrReturnIdOfTagWithSameName(tagForInsertion)
                .blockingGet()
            tagForInsertion.copy(id = id)
        }.toSet()
    }

    private fun getAndInsertAllExpenses(sheet: Sheet, allTags: Set<Tag>): List<Expense> {
        val expensesForInsertion = arrayListOf<Expense>()

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

        for (row in startingRow until sheet.rows) {
            val amount =
                getAmountOrNull(sheet, columnOfAmount, row) ?: continue
            val currency =
                getCurrencyOrNull(sheet, columnOfCurrency, row) ?: continue
            val title =
                getTitleOrNull(sheet, columnOfTitle, row) ?: continue
            val date =
                getDateOrNull(sheet, columnOfDate, row) ?: continue
            val notes =
                getNotesOrNull(sheet, columnOfNotes, row) ?: continue
            val tags =
                getTagsOrNull(sheet, columnOfTags, row, allTags) ?: continue

            expensesForInsertion.add(Expense(0, amount, currency, title, date, notes, 0, 0, tags))
        }

        return expensesForInsertion.map { expenseForInsertion ->
            val id = databaseDataSource.insertExpense(expenseForInsertion).blockingFirst()
            expenseForInsertion.copy(id = id)
        }
    }

    private fun getColumnIndexWithTitle(sheet: Sheet, title: String): Int {
        for (column in 0 until sheet.columns) {
            if (sheet.getCell(column, 0).contents == title) {
                return column
            }
        }

        return INVALID_COLUMN
    }

    private fun getAmountOrNull(sheet: Sheet, column: Int, row: Int): Float? {
        val cell = sheet.getCell(column, row) as? NumberCell ?: return null
        return cell.value.toFloat()
    }

    private fun getCurrencyOrNull(sheet: Sheet, column: Int, row: Int): Currency? {
        val cell = sheet.getCell(column, row) as? LabelCell ?: return null
        return Currency.fromCode(cell.contents)
    }

    private fun getTitleOrNull(sheet: Sheet, column: Int, row: Int): String? {
        val cell = sheet.getCell(column, row) as? LabelCell ?: return null
        return cell.contents
    }

    private fun getDateOrNull(sheet: Sheet, column: Int, row: Int): LocalDate? {
        val cell = sheet.getCell(column, row) as? LabelCell ?: return null
        return cell.contents.toLocalDate(DATE_PATTERN)
    }

    private fun getNotesOrNull(sheet: Sheet, column: Int, row: Int): String? {
        val cell = sheet.getCell(column, row) as? LabelCell ?: return null
        return cell.contents
    }

    private fun getTagsOrNull(sheet: Sheet, column: Int, row: Int, allTags: Set<Tag>): List<Tag>? {
        val cell = sheet.getCell(column, row) as? LabelCell ?: return null
        return cell.contents.split(", ")
            .filter { it.isNotEmpty() }
            .map { name -> allTags.first { it.name == name } }
    }

    companion object {

        private const val TAG = "ExpenseImportWorker"

        private const val KEY_FILE_URI = "fileUri"

        private const val INVALID_COLUMN = -1

        private const val DATE_PATTERN = "yyyy-MM-dd"

        fun enqueue(fileUri: Uri): UUID {
            val data = workDataOf(KEY_FILE_URI to fileUri.toString())

            val request = OneTimeWorkRequestBuilder<ExpenseImportWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance().enqueue(request)

            return request.id
        }
    }
}
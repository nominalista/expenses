package com.nominalista.expenses.settings.work

import android.content.Context
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.success
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.extensions.toDate
import jxl.Workbook
import jxl.write.*
import jxl.write.Number
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ExpenseExportWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val dataStore: DataStore by lazy {
        (applicationContext as Application).defaultDataStore
    }

    override suspend fun doWork() = coroutineScope {
        val workbook = Workbook.createWorkbook(createFile())
        val sheet = createSheet(workbook)

        val expenses = prepareExpenses()
        addContent(sheet, expenses)

        workbook.write()
        workbook.close()

        Log.d(TAG, "Succeeded to export expenses.")
        success()
    }

    private fun createFile(): File {
        val downloads = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
        val appName = applicationContext.getString(R.string.app_name)
        val dateString = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(LocalDateTime.now())
        val fileName = "${appName}_$dateString"
        val extension = ".xls"
        return File(downloads, fileName + extension)
    }

    private fun createSheet(workbook: WritableWorkbook): WritableSheet {
        val name = applicationContext.getString(R.string.expenses)
        return workbook.createSheet(name, 0)
    }

    private fun prepareExpenses(): List<Expense> {
        return dataStore.getExpenses().map { it.sorted() }.blockingGet()
    }

    private fun addContent(sheet: WritableSheet, expenses: List<Expense>) {
        addColumnNames(sheet)
        addExpenses(sheet, expenses)
    }

    private fun addColumnNames(sheet: WritableSheet) {
        val columnNames = createColumnNames()
        columnNames.forEachIndexed { column, columnName ->
            sheet.addCell(Label(column, 0, columnName))
        }
    }

    private fun createColumnNames(): List<String> {
        val list = ArrayList<String>()
        list.add(applicationContext.getString(R.string.column_amount))
        list.add(applicationContext.getString(R.string.column_currency))
        list.add(applicationContext.getString(R.string.column_title))
        list.add(applicationContext.getString(R.string.column_date))
        list.add(applicationContext.getString(R.string.column_notes))
        list.add(applicationContext.getString(R.string.column_tags))
        return list
    }

    private fun addExpenses(sheet: WritableSheet, expenses: List<Expense>) {
        expenses.forEachIndexed { index, expense ->
            val row = index + 1
            addAmount(sheet, row, expense)
            addCurrency(sheet, row, expense)
            addTitle(sheet, row, expense)
            addDate(sheet, row, expense)
            addNotes(sheet, row, expense)
            addTags(sheet, row, expense)
        }
    }

    private fun addAmount(sheet: WritableSheet, row: Int, expense: Expense) {
        // Casting Float to String is a crucial part here. Otherwise some of the values could
        // have to big precision. See: https://bit.ly/2ZlbaqR.
        val amount = expense.amount.toString().toDouble()
        sheet.addCell(Number(COLUMN_AMOUNT, row, amount))
    }

    private fun addCurrency(sheet: WritableSheet, row: Int, expense: Expense) {
        val currency = expense.currency.code
        sheet.addCell(Label(COLUMN_CURRENCY, row, currency))
    }

    private fun addTitle(sheet: WritableSheet, row: Int, expense: Expense) {
        val title = expense.title
        sheet.addCell(Label(COLUMN_TITLE, row, title))
    }

    private fun addDate(sheet: WritableSheet, row: Int, expense: Expense) {
        val date = expense.date.toDate()
        val cellFormat = WritableCellFormat(DateFormat(DATE_PATTERN))
        sheet.addCell(DateTime(COLUMN_DATE, row, date, cellFormat))
    }

    private fun addNotes(sheet: WritableSheet, row: Int, expense: Expense) {
        val notes = expense.notes
        sheet.addCell(Label(COLUMN_NOTES, row, notes))
    }

    private fun addTags(sheet: WritableSheet, row: Int, expense: Expense) {
        val tags = expense.tags.joinToString { it.name }
        sheet.addCell(Label(COLUMN_TAGS, row, tags))
    }

    companion object {

        private const val TAG = "ExpenseExportWorker"

        private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"

        private const val DATE_PATTERN = "yyyy-MM-dd"

        private const val COLUMN_AMOUNT = 0
        private const val COLUMN_CURRENCY = 1
        private const val COLUMN_TITLE = 2
        private const val COLUMN_DATE = 3
        private const val COLUMN_NOTES = 4
        private const val COLUMN_TAGS = 5

        fun enqueue(context: Context): UUID {
            val request = OneTimeWorkRequest.Builder(ExpenseExportWorker::class.java).build()

            WorkManager.getInstance(context).enqueue(request)

            return request.id
        }
    }
}
package com.nominalista.expenses.settings.domain

import android.content.Context
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Completable
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import java.io.File

class ExportExpensesUseCase(
    private val databaseDataSource: DatabaseDataSource
) {

    operator fun invoke(context: Context): Completable {
        return databaseDataSource.getExpenses()
            .map { sort(it) }
            .map { export(context, it) }
            .ignoreElements()
    }

    private fun sort(expenses: List<Expense>) = expenses.sortedBy { it.date.utcTimestamp }

    private fun export(context: Context, expenses: List<Expense>): Boolean {
        val file = createFile(context)
        val workbook = Workbook.createWorkbook(file)
        val sheet = createSheet(context, workbook)
        addContent(context, sheet, expenses)
        workbook.write()
        workbook.close()
        return true
    }

    private fun createFile(context: Context): File {
        val downloads = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
        val appName = context.getString(R.string.app_name)
        val dateString = Date.now().toString(DATE_PATTERN)
        val fileName = "${appName}_$dateString"
        val extension = ".xls"
        return File(downloads, fileName + extension)
    }

    private fun createSheet(context: Context, workbook: WritableWorkbook): WritableSheet {
        val name = context.getString(R.string.expenses)
        return workbook.createSheet(name, 0)
    }

    private fun addContent(context: Context, sheet: WritableSheet, expenses: List<Expense>) {
        addColumnNames(context, sheet)
        addExpenses(sheet, expenses)
    }

    private fun addColumnNames(context: Context, sheet: WritableSheet) {
        val columnNames = createColumnNames(context)
        columnNames.forEachIndexed { column, columnName -> addCell(sheet, 0, column, columnName) }
    }

    private fun createColumnNames(context: Context): List<String> {
        val list = ArrayList<String>()
        list.add(context.getString(R.string.column_amount))
        list.add(context.getString(R.string.column_currency))
        list.add(context.getString(R.string.column_title))
        list.add(context.getString(R.string.column_date))
        list.add(context.getString(R.string.column_notes))
        list.add(context.getString(R.string.column_tags))
        return list
    }

    private fun addExpenses(sheet: WritableSheet, expenses: List<Expense>) {
        expenses.forEachIndexed { index, expense ->
            val expenseColumns = createExpenseColumns(expense)
            expenseColumns.forEachIndexed { column, expenseColumn ->
                addCell(sheet, index + 1, column, expenseColumn)
            }
        }
    }

    private fun createExpenseColumns(expense: Expense): List<String> {
        val list = ArrayList<String>()
        list.add("%.2f".format(expense.amount))
        list.add(expense.currency.code)
        list.add(expense.title)
        list.add(expense.date.toReadableString())
        list.add(expense.notes)
        list.add(expense.tags.joinToString { it.name })
        return list
    }

    private fun addCell(sheet: WritableSheet, row: Int, column: Int, string: String) {
        val label = Label(column, row, string)
        sheet.addCell(label)
    }

    companion object {
        private const val DATE_PATTERN = "yyyyMMdd_HHmmss"
    }
}
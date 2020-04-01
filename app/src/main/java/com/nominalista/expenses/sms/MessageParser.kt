package com.nominalista.expenses.sms

import android.util.Log
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Format
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.data.store.DataStore
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.threeten.bp.LocalDate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern

object MessageParser {
    val formats = listOf(
            Format("1,000.00", groupSeparator = ",", decimalSeparator = "."),
            Format("1.000,00", groupSeparator = ".", decimalSeparator = ","),
            Format("1 000.00", groupSeparator = " ", decimalSeparator = "."),
            Format("1 000,00", groupSeparator = " ", decimalSeparator = ","))

    fun getAmount(message: String, rule: Rule): Double {
        val valueString = getAmountString(rule, message)
        val format = DecimalFormatSymbols(Locale.US)
        format.decimalSeparator = rule.decimalSeparator[0]
        format.groupingSeparator = rule.groupSeparator[0]
        val df = DecimalFormat("", format)
        df.groupingSize = 3
        df.isGroupingUsed = true
        return df.parse(valueString).toDouble()
    }

    private fun getAmountString(rule: Rule, message: String): String {
        val symbol = "\\${rule.firstSymbol}"
        val group = "(?:\\d*\\${rule.groupSeparator})+?"
        val decimal = "(?:\\d*\\${rule.decimalSeparator})?"
        val space = "(?:\\.)?\\ "


        val pattern: Pattern = Pattern.compile(".*" +
                "$symbol(" +
                group +
                decimal +
                "\\d*" +
                ")$space" +
                ".*")
        val reversePattern: Pattern = Pattern.compile(".*" +
                space +
                "(" +
                group +
                decimal +
                "\\d*" +
                ")$symbol" +
                ".*")
        val matcher = pattern.matcher(message)
        val reverseMatcher = reversePattern.matcher(message)
        return when {
            matcher.find() -> matcher.group(1)
            reverseMatcher.find() -> reverseMatcher.group(1)
            else -> ""
        }
    }

    fun insertExpense(localDataStore: DataStore, mainScheduler: Scheduler, ioScheduler: Scheduler, defaultCurrency: Currency, rule: Rule, messageBody: String): Disposable {
        return try {
            val totalDouble = getAmount(messageBody, rule)
            insertExpense(localDataStore, mainScheduler, ioScheduler, defaultCurrency, totalDouble, messageBody)
        } catch (exception: Exception) {
            insertExpense(localDataStore, mainScheduler, ioScheduler, defaultCurrency, 0.0, messageBody)
        }
    }

    fun insertExpense(dataStore: DataStore, mainScheduler: Scheduler, ioScheduler: Scheduler, defaultCurrency: Currency, totalDouble: Double, messageBody: String): Disposable {
        return dataStore.insertExpense(prepareExpenseForInsertion(totalDouble, messageBody, defaultCurrency)).subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    Log.d("SMS service", "Expense insertion succeeded.")
                }, { error ->
                    Log.e("SMS service", "Expense insertion failed (${error.message}).")
                })
    }

    private fun prepareExpenseForInsertion(amount: Double, notes: String, defaultCurrency: Currency): Expense {

        return Expense(
                "",
                amount,
                defaultCurrency,
                "",
                emptyList(),
                LocalDate.now(),
                notes,
                null
        )
    }
}
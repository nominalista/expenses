package com.nominalista.expenses.sms

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern

class SmsService : IntentService("com.nominalista.expenses.sms.SmsService") {
    var message: String? = null
    private val compositeDisposable = CompositeDisposable()

    companion object {
        fun getIntent(context: Context, message: String): Intent {
            val i = Intent(context, SmsService::class.java)
            i.putExtra("EXTRA", message)
            return i
        }
    }

    override fun onHandleIntent(intent: Intent) {
        try {
            intent.extras?.let {
                val messageBody: String = it["EXTRA"] as String
                message = messageBody
                val rule = getRule(applicationContext, messageBody)
                val totalDouble = getAmount(messageBody, rule)
                insertExpense(totalDouble, messageBody)
            }
        } catch (exception: Exception) {
            insertExpense(0.0, message.toString())
        }
    }

    private fun insertExpense(totalDouble: Double, messageBody: String) {
        val dataStore = applicationContext.application.localDataStore
        compositeDisposable += dataStore.insertExpense(prepareExpenseForInsertion(totalDouble, messageBody)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("SMS service", "Expense insertion succeeded.")
                }, { error ->
                    Log.e("SMS service", "Expense insertion failed (${error.message}).")
                })
    }

    private fun prepareExpenseForInsertion(amount: Double, notes: String): Expense {
        val defaultCurrency = applicationContext.application.preferenceDataSource.getDefaultCurrency(applicationContext.application)

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

    private fun getRule(context: Context, message: String): Rule {
        val dataStore = context.application.localDataStore
        val rules = dataStore.getRules()
                .subscribeOn(Schedulers.io())
                .blockingGet()
        return rules.first { it.keywords.map { keyword -> message.contains(keyword, ignoreCase = true) }.fold(false) { acc, next -> acc || next } }
    }

    private fun getAmount(message: String, rule: Rule): Double {
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
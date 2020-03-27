package com.nominalista.expenses.sms

import com.nominalista.expenses.data.model.Format
import com.nominalista.expenses.data.model.Rule
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
}
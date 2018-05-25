package com.nominalista.expenses.infrastructure.extensions

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.*

// Fields

fun Date.get(field: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(field)
}

// Printing

fun Date.toString(pattern: String): String {
    @SuppressLint("SimpleDateFormat")
    val format = SimpleDateFormat(pattern)
    return format.format(this)
}

fun Date.monthName(context: Context): String? {
    return monthName(context, "MMMM")
}

fun Date.monthShortName(context: Context): String? {
    return monthName(context, "MMM")
}

private fun Date.monthName(context: Context, pattern: String): String? {
    val localeListCompat = ConfigurationCompat.getLocales(context.resources.configuration)
    val locale = localeListCompat.default() ?: return null
    return SimpleDateFormat(pattern, locale).format(this)
}
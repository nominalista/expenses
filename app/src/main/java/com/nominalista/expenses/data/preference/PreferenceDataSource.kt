package com.nominalista.expenses.data.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.home.presentation.DateRange

class PreferenceDataSource {

    fun getDefaultCurrency(context: Context): Currency {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val key = getDefaultCurrencyKey(context)

        return preferences.getString(key, null)?.let { Currency.valueOf(it) } ?: Currency.USD
    }

    fun setDefaultCurrency(context: Context, defaultCurrency: Currency) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val key = getDefaultCurrencyKey(context)

        preferences.edit().putString(key, defaultCurrency.name).apply()
    }

    fun getDateRange(context: Context): DateRange {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val key = getDateRangeKey(context)

        return preferences.getString(key, null)?.let { DateRange.valueOf(it) } ?: DateRange.ALL_TIME
    }

    fun setDateRange(context: Context, dateRange: DateRange) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val key = getDateRangeKey(context)

        preferences.edit().putString(key, dateRange.name).apply()
    }

    private fun getDefaultCurrencyKey(context: Context) =
        context.getString(R.string.key_default_currency)

    private fun getDateRangeKey(context: Context) =
        context.getString(R.string.key_date_range)
}
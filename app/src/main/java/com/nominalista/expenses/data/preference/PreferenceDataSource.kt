package com.nominalista.expenses.data.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.Theme
import com.nominalista.expenses.data.model.Currency
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

    fun getIsUserOnboarded(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getIsUserOnboardedKey(context)
        return preferences.getBoolean(key, false)
    }

    fun setIsUserOnboarded(context: Context, isUserOnboarded: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getIsUserOnboardedKey(context)
        preferences.edit().putBoolean(key, isUserOnboarded).apply()
    }

    fun getTheme(context: Context): Theme {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getThemeKey(context)
        return preferences.getString(key, null)?.let { Theme.valueOf(it) } ?: Theme.SYSTEM_DEFAULT
    }

    fun setTheme(context: Context, theme: Theme) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getThemeKey(context)
        preferences.edit().putString(key, theme.name).apply()
    }


    fun getIsSmsReaderEnabled(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getSmsModeKey(context)
        return preferences.getBoolean(key, false)
    }

    fun setIsSmsReaderEnabled(context: Context, enabled: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = getSmsModeKey(context)
        preferences.edit().putBoolean(key, enabled).apply()
    }

    private fun getDefaultCurrencyKey(context: Context) =
        context.getString(R.string.key_default_currency)

    private fun getDateRangeKey(context: Context) =
        context.getString(R.string.key_date_range)

    private fun getIsUserOnboardedKey(context: Context) =
        context.getString(R.string.key_is_user_onboarded)

    private fun getThemeKey(context: Context) =
        context.getString(R.string.key_theme)

    private fun getSmsModeKey(context: Context) =
            context.getString(R.string.key_sms_reader)

}
package com.nominalista.expenses.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.utils.CurrencyConverter
import com.nominalista.expenses.data.Currency
import io.reactivex.Completable
import io.reactivex.Observable

class PreferenceDataSource {

    fun getDefaultCurrency(context: Context): Observable<Currency> {
        return Observable.defer {
            val preferences = getPreferences(context)
            val key = context.getString(R.string.key_default_currency)
            val currencyIfNotSet = Currency.USD
            val defaultCurrencyString = preferences.getString(key,
                    CurrencyConverter.toString(currencyIfNotSet))
            Observable.just(CurrencyConverter.toCurrency(defaultCurrencyString) ?: currencyIfNotSet)
        }
    }

    fun setDefaultCurrency(context: Context, currency: Currency): Completable {
        return Completable.defer {
            val preferences = getPreferences(context)
            val key = context.getString(R.string.key_default_currency)
            val value = CurrencyConverter.toString(currency)
            with(preferences.edit()) {
                putString(key, value)
                apply()
            }
            Completable.complete()
        }
    }

    private fun getPreferences(context: Context): SharedPreferences {
        val preferencesKey = context.getString(R.string.key_preferences)
        return context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)
    }
}
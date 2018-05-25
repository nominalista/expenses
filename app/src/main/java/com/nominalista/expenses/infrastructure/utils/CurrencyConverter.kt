package com.nominalista.expenses.infrastructure.utils

import androidx.room.TypeConverter
import com.nominalista.expenses.model.Currency

class CurrencyConverter {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toCurrency(string: String): Currency? {
            return Currency.fromCode(string)
        }

        @JvmStatic
        @TypeConverter
        fun toString(currency: Currency): String? {
            return currency.toString()
        }
    }
}
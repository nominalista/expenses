package com.nominalista.expenses.infrastructure.utils

import androidx.room.TypeConverter
import com.nominalista.expenses.data.Date

class DateConverter {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toDate(long: Long) = Date(long)

        @JvmStatic
        @TypeConverter
        fun toLong(date: Date) = date.utcTimestamp
    }
}
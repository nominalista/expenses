package com.nominalista.expenses.infrastructure.utils

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toDate(long: Long): Date? = Date(long)

        @JvmStatic
        @TypeConverter
        fun toString(date: Date): Long? = date.time
    }
}
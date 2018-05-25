package com.nominalista.expenses.infrastructure.utils

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

val pattern = "dd-MM-yyyy"

class DateConverter {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toDate(string: String): Date? {
            @SuppressLint("SimpleDateFormat")
            val format = SimpleDateFormat(pattern)
            return format.parse(string)
        }

        @JvmStatic
        @TypeConverter
        fun toString(date: Date): String? {
            @SuppressLint("SimpleDateFormat")
            val format = SimpleDateFormat(pattern)
            return format.format(date)
        }
    }
}
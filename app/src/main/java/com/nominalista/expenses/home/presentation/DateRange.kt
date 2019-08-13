package com.nominalista.expenses.home.presentation

import com.nominalista.expenses.util.extensions.*
import org.threeten.bp.LocalDate

enum class DateRange {

    TODAY {
        override fun contains(date: LocalDate): Boolean {
            return LocalDate.now().isEqual(date)
        }
    },

    THIS_WEEK {
        override fun contains(date: LocalDate): Boolean {
            val now = LocalDate.now()
            val dayBeforeFirstDayOfWeek = now.firstDayOfWeek().yesterday()
            val dayAfterLastDayOfWeek = now.lastDayOfWeek().tomorrow()
            return date.isAfter(dayBeforeFirstDayOfWeek) && date.isBefore(dayAfterLastDayOfWeek)
        }
    },

    THIS_MONTH {
        override fun contains(date: LocalDate): Boolean {
            val now = LocalDate.now()
            val dayBeforeFirstDayOfMonth = now.firstDayOfMonth().yesterday()
            val dayAfterLastDayOfMonth = now.lastDayOfMonth().tomorrow()
            return date.isAfter(dayBeforeFirstDayOfMonth) && date.isBefore(dayAfterLastDayOfMonth)
        }
    },

    ALL_TIME {
        override fun contains(date: LocalDate) = true
    };

    abstract fun contains(date: LocalDate): Boolean
}
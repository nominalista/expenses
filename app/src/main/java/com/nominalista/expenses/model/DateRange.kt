package com.nominalista.expenses.model

import java.util.*

enum class DateRange {

    Today,
    ThisWeek,
    ThisMonth,
    AllTime;

    fun contains(date: Date): Boolean {
        val start = getStart()
        val now = getNow()
        return date.time in start..now
    }

    private fun getStart(): Long {
        return when (this) {
            Today -> todayStart()
            ThisWeek -> thisWeekStart()
            ThisMonth -> thisMonthStart()
            AllTime -> allTimeStart()
        }
    }

    private fun todayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.clear(Calendar.MILLISECOND)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MINUTE)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        return calendar.timeInMillis
    }

    private fun thisWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.clear(Calendar.MILLISECOND)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MINUTE)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.DAY_OF_WEEK, 1)
        return calendar.timeInMillis
    }

    private fun thisMonthStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.clear(Calendar.MILLISECOND)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MINUTE)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.DAY_OF_WEEK, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    private fun allTimeStart(): Long {
        return 0
    }

    private fun getNow(): Long {
        return Calendar.getInstance().timeInMillis
    }
}
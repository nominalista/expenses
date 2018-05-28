package com.nominalista.expenses.ui.home

import java.util.*
import java.util.Calendar.*

enum class DateRange {

    Today {
        override fun getStart(): Long {
            return getTodayStart().timeInMillis
        }

        override fun getEnd(): Long {
            val calendar = getTodayStart()
            calendar.add(DATE, 1)
            return calendar.timeInMillis
        }

        private fun getTodayStart(): Calendar {
            val calendar = Calendar.getInstance()
            calendar.clear(MILLISECOND)
            calendar.clear(SECOND)
            calendar.clear(MINUTE)
            calendar.set(HOUR_OF_DAY, 0)
            return calendar
        }
    },
    ThisWeek {
        override fun getStart(): Long {
            val calendar = getTodayTruncated()
            calendar.set(Calendar.DAY_OF_WEEK, 1)
            return calendar.timeInMillis
        }

        override fun getEnd(): Long {
            val calendar = getTodayTruncated()
            calendar.set(Calendar.DAY_OF_WEEK, 7)
            return calendar.timeInMillis
        }

        private fun getTodayTruncated(): Calendar {
            val calendar = Calendar.getInstance()
            calendar.clear(MILLISECOND)
            calendar.clear(SECOND)
            calendar.clear(MINUTE)
            calendar.set(HOUR_OF_DAY, 0)
            return calendar
        }
    },
    ThisMonth {
        override fun getStart(): Long {
            return getMonthStart().timeInMillis
        }

        override fun getEnd(): Long {
            val calendar = getMonthStart()
            calendar.add(MONTH, 1)
            return calendar.timeInMillis
        }

        private fun getMonthStart(): Calendar {
            val calendar = Calendar.getInstance()
            calendar.clear(Calendar.MILLISECOND)
            calendar.clear(Calendar.SECOND)
            calendar.clear(Calendar.MINUTE)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.DAY_OF_WEEK, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            return calendar
        }
    },
    AllTime {
        override fun getStart(): Long {
            return 0
        }

        override fun getEnd(): Long {
            return Long.MAX_VALUE
        }
    };

    abstract fun getStart(): Long

    abstract fun getEnd(): Long

    fun contains(date: Date): Boolean {
        val start = getStart()
        val now = getEnd()
        return date.time in start..now
    }
}
package com.nominalista.expenses.userinterface.home

import com.nominalista.expenses.data.Date

enum class DateRange {

    Today {
        override fun contains(date: Date): Boolean {
            val now = Date.now()
            val start = now.withTruncatedTime()
            val end = now.tomorrow().withTruncatedTime()
            return date.utcTimestamp in start.utcTimestamp until end.utcTimestamp
        }
    },

    ThisWeek {
        override fun contains(date: Date): Boolean {
            val now = Date.now()
            val start = now.firstDayOfWeek().withTruncatedTime()
            val end = now.lastDayOfWeek().tomorrow().withTruncatedTime()
            return date.utcTimestamp in start.utcTimestamp until end.utcTimestamp
        }
    },

    ThisMonth {
        override fun contains(date: Date): Boolean {
            val now = Date.now()
            val start = now.firstDayOfMonth().withTruncatedTime()
            val end = now.lastDayOfMonth().tomorrow().withTruncatedTime()
            return date.utcTimestamp in start.utcTimestamp until end.utcTimestamp
        }
    },

    AllTime {
        override fun contains(date: Date) = true
    };

    abstract fun contains(date: Date): Boolean
}
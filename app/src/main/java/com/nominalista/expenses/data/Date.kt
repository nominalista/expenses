package com.nominalista.expenses.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields

@Parcelize
data class Date(val utcTimestamp: Long) : Parcelable {

    companion object {

        fun now(): Date {
            val dateTime = ZonedDateTime.now()
            val instant = dateTime.toInstant()
            return Date(instant.toEpochMilli())
        }

        fun from(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Date {
            val zoneId = ZoneId.systemDefault()
            val dateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, zoneId)
            val instant = dateTime.toInstant()
            return Date(instant.toEpochMilli())
        }

        private fun from(zonedDateTime: ZonedDateTime): Date {
            val instant = zonedDateTime.toInstant()
            return Date(instant.toEpochMilli())
        }
    }

    val year get() = dateTime.year
    val month get() = dateTime.month
    val day get() = dateTime.dayOfMonth
    val hour get() = dateTime.hour
    val minute get() = dateTime.minute
    val second get() = dateTime.second

    private val dateTime get() = Instant.ofEpochMilli(utcTimestamp).atZone(ZoneId.systemDefault())

    // Truncated

    fun withTruncatedTime() = Date.from(dateTime.truncatedTo(ChronoUnit.DAYS))

    // First or last day

    fun yesterday() = Date.from(dateTime.minusDays(1))

    fun tomorrow() = Date.from(dateTime.plusDays(1))

    fun firstDayOfWeek() = Date.from(dateTime.with(WeekFields.ISO.dayOfWeek(), 1))

    fun lastDayOfWeek() = Date.from(dateTime.with(WeekFields.ISO.dayOfWeek(), 7))

    fun firstDayOfMonth() = Date.from(dateTime.with(TemporalAdjusters.firstDayOfMonth()))

    fun lastDayOfMonth() = Date.from(dateTime.with(TemporalAdjusters.lastDayOfMonth()))

    // Printing

    fun toReadableString() = toString("EEEE, d MMM H:mm")

    fun toString(pattern: String) = DateTimeFormatter.ofPattern(pattern).format(dateTime)
}
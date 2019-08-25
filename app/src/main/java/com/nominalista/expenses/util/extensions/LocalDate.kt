package com.nominalista.expenses.util.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.*

// Formatting

fun LocalDate.toString(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(this)

// Timestamp

fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

// Accessing common dates

fun LocalDate.yesterday(): LocalDate = minusDays(1)

fun LocalDate.tomorrow(): LocalDate = plusDays(1)

fun LocalDate.firstDayOfWeek(): LocalDate = with(WeekFields.ISO.dayOfWeek(), 1)

fun LocalDate.lastDayOfWeek(): LocalDate = with(WeekFields.ISO.dayOfWeek(), 7)

fun LocalDate.firstDayOfMonth(): LocalDate = with(TemporalAdjusters.firstDayOfMonth())

fun LocalDate.lastDayOfMonth(): LocalDate = with(TemporalAdjusters.lastDayOfMonth())

// Support for old format

fun LocalDate.toDate(): Date = GregorianCalendar(year, monthValue - 1, dayOfMonth).time
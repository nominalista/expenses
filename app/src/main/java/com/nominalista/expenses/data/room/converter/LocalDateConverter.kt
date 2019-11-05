package com.nominalista.expenses.data.room.converter

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset

class LocalDateConverter {

    /**
     * First convert time since epoch to the ZonedDateTime. Epoch time is always in UTC.
     * Then convert ZonedDateTime to LocalDate.
     *
     * Example:
     * 946684800 -> 2000-01-01T00:00:00+00:00 -> 2000-01-01.
     */
    @TypeConverter
    fun toLocalDate(long: Long): LocalDate =
        Instant.ofEpochMilli(long).atZone(ZoneOffset.UTC).toLocalDate()

    /**
     * First convert LocalDate to the ZonedDateTime. LocalDate doesn't hold zone information, so we
     * can assume that the converted date is in UTC. Then convert ZonedDateTime to time since epoch.
     *
     * Example:
     * 2000-01-01 -> 2000-01-01T00:00:00+00:00 -> 946684800.
     */
    @TypeConverter
    fun fromLocalDate(localDate: LocalDate): Long =
        localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
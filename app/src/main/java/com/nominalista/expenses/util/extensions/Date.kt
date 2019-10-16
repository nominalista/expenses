package com.nominalista.expenses.util.extensions

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.util.*

// Support for new format

fun Date.toLocalDate(): LocalDate = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC).toLocalDate()
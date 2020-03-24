package com.nominalista.expenses.util

import org.threeten.bp.ZonedDateTime

const val READABLE_DATE_FORMAT = "EEEE, d MMMM"

fun getCurrentTimestamp() = ZonedDateTime.now().toInstant().toEpochMilli()
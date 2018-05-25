package com.nominalista.expenses.infrastructure.extensions

import androidx.core.os.LocaleListCompat
import java.util.*

fun LocaleListCompat.default(): Locale? = get(0)
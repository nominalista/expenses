package com.nominalista.expenses.infrastructure.extensions

import java.util.*

val Calendar.truncatedTime: Date
    get() {
        val calendar = Calendar.getInstance()
        calendar.set(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        return calendar.time
    }
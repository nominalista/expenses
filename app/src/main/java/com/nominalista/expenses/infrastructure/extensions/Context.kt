package com.nominalista.expenses.infrastructure.extensions

import android.content.Context
import com.nominalista.expenses.Application

val Context.application: Application
    get() = applicationContext as Application
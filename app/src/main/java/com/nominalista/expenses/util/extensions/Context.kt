package com.nominalista.expenses.util.extensions

import android.content.Context
import com.nominalista.expenses.Application

val Context.application: Application
    get() = applicationContext as Application
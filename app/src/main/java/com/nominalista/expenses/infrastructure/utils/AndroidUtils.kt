package com.nominalista.expenses.infrastructure.utils

import android.os.AsyncTask

fun runOnBackground(function: () -> Unit) {
    AsyncTask.execute({ function() })
}
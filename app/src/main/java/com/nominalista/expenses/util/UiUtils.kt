package com.nominalista.expenses.util

import android.os.Handler

fun runOnUiThread(delayMillis: Long = 0, block: () -> Unit) {
    Handler().postDelayed(block, delayMillis)
}
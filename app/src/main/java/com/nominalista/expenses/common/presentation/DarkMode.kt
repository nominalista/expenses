package com.nominalista.expenses.common.presentation

import androidx.appcompat.app.AppCompatDelegate

enum class DarkMode {
    ON, OFF, SYSTEM_DEFAULT;

    fun toNightMode(): Int = when (this) {
        ON -> AppCompatDelegate.MODE_NIGHT_YES
        OFF -> AppCompatDelegate.MODE_NIGHT_NO
        SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
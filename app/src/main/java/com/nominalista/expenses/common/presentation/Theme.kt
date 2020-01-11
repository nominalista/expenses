package com.nominalista.expenses.common.presentation

import androidx.appcompat.app.AppCompatDelegate

enum class Theme {
    LIGHT, DARK, SYSTEM_DEFAULT;

    fun toNightMode(): Int = when (this) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
        SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
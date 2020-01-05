package com.nominalista.expenses.data.model

import androidx.appcompat.app.AppCompatDelegate

enum class Theme {
    LIGHT, DARK, AUTO;

    fun toNightMode(): Int = when (this) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
        AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    companion object {

        fun fromNightMode(nightMode: Int) = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> DARK
            else -> AUTO
        }
    }
}
package com.nominalista.expenses.configuration

interface Configuration {

    fun enqueueSync()

    fun getBoolean(key: String): Boolean

    fun getString(key: String): String

    companion object {
        const val KEY_BANNER_ENABLED = "banner_enabled"
        const val KEY_BANNER_TITLE = "banner_title"
        const val KEY_BANNER_SUBTITLE = "banner_subtitle"
    }
}
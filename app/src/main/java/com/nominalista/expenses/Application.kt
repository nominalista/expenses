package com.nominalista.expenses

import com.jakewharton.threetenabp.AndroidThreeTen
import com.nominalista.expenses.data.database.ApplicationDatabase

class Application : android.app.Application() {

    val database by lazy { ApplicationDatabase.build(this) }

    override fun onCreate() {
        super.onCreate()
        initializeThreeTeen()
    }

    private fun initializeThreeTeen() {
        AndroidThreeTen.init(this)
    }
}

package com.nominalista.expenses

import androidx.room.Room
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nominalista.expenses.data.database.ApplicationDatabase

private const val DATABASE_NAME = "database"

class Application : android.app.Application() {

    lateinit var database: ApplicationDatabase

    override fun onCreate() {
        super.onCreate()
        initializeThreeTeen()
        initializeDatabase()
    }

    private fun initializeThreeTeen() {
        AndroidThreeTen.init(this)
    }

    private fun initializeDatabase() {
        database = Room.databaseBuilder(this, ApplicationDatabase::class.java, DATABASE_NAME).build()
    }
}

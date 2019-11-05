package com.nominalista.expenses

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nominalista.expenses.data.room.ApplicationDatabase

class Application : android.app.Application() {

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    val database by lazy { ApplicationDatabase.build(this) }

    val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate() {
        super.onCreate()
        initializeThreeTeen()
    }

    private fun initializeThreeTeen() {
        AndroidThreeTen.init(this)
    }
}

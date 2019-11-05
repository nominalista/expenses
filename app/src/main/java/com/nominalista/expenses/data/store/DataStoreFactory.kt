package com.nominalista.expenses.data.store

import com.nominalista.expenses.Application
import com.nominalista.expenses.authentication.AuthenticationManager
import com.nominalista.expenses.data.firebase.FirebaseDataStore
import com.nominalista.expenses.data.room.RoomDataStore

object DataStoreFactory {

    fun get(application: Application): DataStore {
        val shouldUseFirebase = AuthenticationManager.getInstance(application).isUserSignedIn()

        return if (shouldUseFirebase ) {
            FirebaseDataStore.getInstance(application)
        } else {
            RoomDataStore.getInstance(application)
        }
    }
}
package com.nominalista.expenses.configuration

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.nominalista.expenses.BuildConfig
import com.nominalista.expenses.R

class FirebaseConfiguration(
    private val remoteConfig: FirebaseRemoteConfig
) : Configuration {

    init {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
            .build()

        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.configuration_defaults)
    }

    override fun enqueueSync() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Succeeded to fetch and active configuration.")

                if (task.result == true) {
                    Log.d(TAG, "Configuration has changed.")
                }
            } else {
                Log.w(TAG, "Failed to fetch and activate configuration: ${task.exception}.")
            }
        }
    }

    override fun getBoolean(key: String) = remoteConfig.getBoolean(key)

    override fun getString(key: String) = remoteConfig.getString(key)

    companion object {
        private const val TAG = "FirebaseConfiguration"

        private val MINIMUM_FETCH_INTERVAL_SECONDS = if (BuildConfig.DEBUG) 60L else 3600L
    }
}
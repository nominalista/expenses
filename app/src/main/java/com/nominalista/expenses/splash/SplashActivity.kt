package com.nominalista.expenses.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.home.presentation.HomeActivity
import com.nominalista.expenses.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (isUserOnboarded()) {
            HomeActivity.start(this)
        } else {
            OnboardingActivity.start(this)
        }

        finish()
    }

    private fun isUserOnboarded(): Boolean =
        (application as Application).preferenceDataSource.getIsUserOnboarded(applicationContext)
}
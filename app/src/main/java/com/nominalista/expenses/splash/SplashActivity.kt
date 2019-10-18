package com.nominalista.expenses.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.authentication.AuthenticationManager
import com.nominalista.expenses.home.presentation.HomeActivity
import com.nominalista.expenses.onboarding.OnboardingActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (isUserSignedIn()) {
            HomeActivity.start(this)
        } else {
            OnboardingActivity.start(this)
        }

        finish()
    }

    private fun isUserSignedIn(): Boolean {
        val authenticationManager = AuthenticationManager.getInstance(application as Application)
        return authenticationManager.isUserSignedIn()
    }
}
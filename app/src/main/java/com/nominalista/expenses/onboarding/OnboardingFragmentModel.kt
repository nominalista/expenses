package com.nominalista.expenses.onboarding

import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.authentication.AuthenticationManager
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import io.reactivex.disposables.CompositeDisposable

class OnboardingFragmentModel(
    application: Application,
    private val authenticationManager: AuthenticationManager
) : AndroidViewModel(application) {

    val requestGoogleSignIn = DataEvent<Intent>()
    val showTestVersionPromptAndNavigateHome = Event()

    private val disposables = CompositeDisposable()

    fun continueWithGoogleRequested() {
        requestGoogleSignIn.next(authenticationManager.getGoogleSignInRequest())
    }

    fun handleGoogleSignInResult(result: Intent) {
        disposables += authenticationManager.handleGoogleSignInResult(result)
            .subscribe({
                Log.d(TAG, "Succeeded to sign in with Google.")
                DataMigrationWorker.enqueue(getApplication())
                showTestVersionPromptAndNavigateHome.next()
            }, { error ->
                Log.w(TAG, "Failed to sign in with Google, cause: ($error).")
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val authenticationManager = AuthenticationManager.getInstance(application)
            return OnboardingFragmentModel(application, authenticationManager) as T
        }
    }

    companion object {
        private const val TAG = "OnboardingFragmentModel"
    }
}
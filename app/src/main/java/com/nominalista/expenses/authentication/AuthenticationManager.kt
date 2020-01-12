package com.nominalista.expenses.authentication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.util.reactive.ReactiveTaskListener
import io.reactivex.Completable
import io.reactivex.Single

class AuthenticationManager(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Current user

    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun getCurrentUserName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    // Sign in

    fun getGoogleSignInRequest(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleGoogleSignInResult(googleSignInResult: Intent): Completable {
        return getCredential(googleSignInResult).flatMapCompletable { signIn(it) }
    }

    private fun getCredential(googleSignInResult: Intent): Single<AuthCredential> {
        val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(googleSignInResult)
        return Single.create(ReactiveTaskListener(getAccountTask))
            .map { GoogleAuthProvider.getCredential(it.idToken, null) }
    }

    private fun signIn(credential: AuthCredential): Completable {
        val signInTask = firebaseAuth.signInWithCredential(credential)
        return Completable.create(ReactiveTaskListener(signInTask))
    }

    // Sign out

    fun signOut(): Completable {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Log.w(TAG, "Current user is null. No need to sign out.")
            return Completable.complete()
        }

        firebaseAuth.signOut()

        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account == null) {
            // User signed in anonymously. No need to sign out from Google.
            Completable.complete()
        } else {
            val signOutFromGoogle = googleSignInClient.signOut()
            Completable.create(ReactiveTaskListener(signOutFromGoogle))
        }
    }

    companion object {
        private const val TAG = "AuthenticationManager"
    }
}
package com.nominalista.expenses.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.nominalista.expenses.R
import com.nominalista.expenses.home.presentation.HomeActivity
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment : Fragment() {

    private lateinit var model: OnboardingFragmentModel

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupModel()
        bindModel()
    }

    private fun setupListeners() {
        buttonCancel.setOnClickListener {
            model.continueWithoutSigningInRequested()
        }
        buttonContinueWithGoogle.setOnClickListener {
            model.continueWithGoogleRequested()
        }
    }

    private fun setupModel() {
        val factory = OnboardingFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(OnboardingFragmentModel::class.java)
    }

    private fun bindModel() {
        compositeDisposable += model.isLoading
            .toObservable()
            .subscribe { enableOrDisableButtons(it) }
        compositeDisposable += model.requestGoogleSignIn
            .toObservable()
            .subscribe { requestGoogleSignIn(it) }
        compositeDisposable += model.navigateToHome
            .toObservable()
            .subscribe { navigateToHome() }
    }

    private fun enableOrDisableButtons(isLoading: Boolean) {
        buttonCancel.isEnabled = !isLoading
        buttonContinueWithGoogle.isEnabled = !isLoading
    }

    private fun requestGoogleSignIn(intent: Intent) {
        startActivityForResult(intent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    private fun navigateToHome() {
        HomeActivity.start(requireContext())
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindModel()
    }

    private fun unbindModel() {
        compositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN && data != null) {
            model.handleGoogleSignInResult(data)
        }
    }

    companion object {
        private const val REQUEST_CODE_GOOGLE_SIGN_IN = 1
    }
}
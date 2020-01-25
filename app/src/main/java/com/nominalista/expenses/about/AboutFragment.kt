package com.nominalista.expenses.about

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetails
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nominalista.expenses.R
import com.nominalista.expenses.util.extensions.getSupportActionBarOrNull
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.extensions.requireApplication
import com.nominalista.expenses.util.extensions.startActivitySafely
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {
    private val disposables = CompositeDisposable()

    private lateinit var model: AboutFragmentModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupModel()
    }

    private fun setupModel() {
        val factory = AboutFragmentModel.Factory(requireApplication())
        model = ViewModelProviders.of(this, factory).get(AboutFragmentModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        setupListeners()
        bindModel()
    }

    private fun setupActionBar() {
        val actionBar = getSupportActionBarOrNull() ?: return
        actionBar.setTitle(R.string.about)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupListeners() {
        contactButton.setOnClickListener { model.contactRequested() }
        rateLayout.setOnClickListener { model.rateRequested() }
        translateLayout.setOnClickListener { model.translateRequested() }
        donateLayout.setOnClickListener { model.donateRequested() }
    }

    private fun bindModel() {
        disposables += model.showTranslationDescription.subscribe(::showTranslationDescription)
        disposables += model.showDonationDescription.subscribe(::showDonationDescription)
        disposables += model.showDonationThanks.subscribe(::showDonationThanks)
        disposables += model.showActivity.subscribe(::showActivity)
        disposables += model.launchBillingFlow.subscribe(::launchBillingFlow)
    }

    private fun showTranslationDescription() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.translate_app)
            .setMessage(R.string.translation_description)
            .setPositiveButton(R.string.contact_me) { _, _ -> model.contactRequested() }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun showDonationDescription() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.donate_app)
            .setMessage(R.string.donate_description)
            .setNegativeButton(R.string.cancel) { _, _ -> }

        val tipDetails = model.tipDetails
        if (tipDetails != null) {
            val positiveButtonText = getString(R.string.donate_with_price, tipDetails.price)
            dialogBuilder.setPositiveButton(positiveButtonText) { _, _ ->
                model.purchaseTipRequested()
            }
            dialogBuilder.show()
        } else {
            dialogBuilder.setPositiveButton(R.string.purchase_unavailable) { _, _ -> }
            val dialog = dialogBuilder.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
    }

    private fun showDonationThanks() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.donation_thanks_title)
            .setMessage(R.string.donation_thanks_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .show()
    }

    private fun showActivity(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireActivity().startActivitySafely(intent)
    }

    private fun launchBillingFlow(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        val responseCode = model.billingClient.launchBillingFlow(requireActivity(), flowParams)

        Log.d(TAG, "Billing flow response code: ${responseCode}.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    companion object {
        private const val TAG = "AboutFragment"
    }
}
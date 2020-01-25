package com.nominalista.expenses.about

import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.nominalista.expenses.Application
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event

class AboutFragmentModel(application: Application) : AndroidViewModel(application),
    PurchasesUpdatedListener {
    val billingClient = BillingClient.newBuilder(application)
        .enablePendingPurchases()
        .setListener(this)
        .build()
    var tipDetails: SkuDetails? = null

    val showTranslationDescription = Event()
    val showDonationDescription = Event()
    val showDonationThanks = Event()

    val showActivity = DataEvent<Uri>()
    val launchBillingFlow = DataEvent<SkuDetails>()

    init {
        connectWithGooglePlayBilling()
    }

    private fun connectWithGooglePlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(result: BillingResult) {
                when (val responseCode = result.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.d(TAG, "Succeeded to setup billing client.")
                        queryForTipDetails()
                    }
                    else -> {
                        Log.w(TAG, "Failed to setup billing client, response code: $responseCode.")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing client disconnected.")
            }
        })
    }

    private fun queryForTipDetails() {
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(PRODUCT_ID_TIP))
            .setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(skuDetailsParams.build()) { result, skuDetailsList ->
            when (val responseCode = result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "Succeeded to query sku details.")
                    tipDetails = skuDetailsList.firstOrNull()
                }
                else -> {
                    Log.w(TAG, "Failed to query sku details, response code: $responseCode.")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectFromGooglePlayBilling()
    }

    private fun disconnectFromGooglePlayBilling() {
        billingClient.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (val responseCode = result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Succeeded to update purchases.")
                purchases?.firstOrNull()?.let { handlePurchase(it) }
            }
            else -> {
                Log.w(TAG, "Failed to update purchases, response code: $responseCode.")
            }
        }
    }

    /**
     * Consume purchase so the user will be able to buy the product again.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(params) { billingResult, _ ->
                when (val responseCode = billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.d(TAG, "Succeeded to consume purchase.")
                        showDonationThanks.next()
                    }
                    else -> {
                        Log.w(TAG, "Failed to consume purchase, response code: $responseCode.")
                    }
                }
            }
        }
    }

    fun contactRequested() {
        showActivity.next(EMAIL_URI)
    }

    fun rateRequested() {
        showActivity.next(GOOGLE_PLAY_URI)
    }

    fun translateRequested() {
        showTranslationDescription.next()
    }

    fun donateRequested() {
        showDonationDescription.next()
    }

    fun purchaseTipRequested() {
        tipDetails?.let { launchBillingFlow.next(it) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AboutFragmentModel(application) as T
        }
    }

    companion object {
        private const val TAG = "AboutFragmentModel"
        private const val PRODUCT_ID_TIP = "tip"
        private val EMAIL_URI = Uri.parse("mailto:the.nominalista@gmail.com")
        private val GOOGLE_PLAY_URI =
            Uri.parse("https://play.google.com/store/apps/details?id=com.nominalista.expenses")
    }
}
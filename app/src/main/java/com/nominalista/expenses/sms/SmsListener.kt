package com.nominalista.expenses.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.util.extensions.application
import io.reactivex.schedulers.Schedulers.io


class SmsListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isSmsReaderEnabled = PreferenceDataSource().getIsSmsReaderEnabled(context)
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action && isSmsReaderEnabled) {
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val messageBody = smsMessage.messageBody
                if (containsKeyword(context, messageBody)) {
                    val i = SmsService.getIntent(context, messageBody)
                    context.startService(i)
                }
            }
        }
    }

    private fun containsKeyword(context: Context, message: String): Boolean {
        val dataStore = context.application.localDataStore
        val rules = dataStore.getRules()
                .subscribeOn(io())
                .blockingGet()
        val keywords: List<String> = rules.map { it.keywords }.reduce { acc, list ->
            val n = acc.toMutableList()
            n.addAll(list)
            n
        }
        return keywords.map { message.contains(it, ignoreCase = true) }.fold(false) { acc, next -> acc || next }
    }
}
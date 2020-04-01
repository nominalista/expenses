package com.nominalista.expenses.sms

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.sms.MessageParser.getAmount
import com.nominalista.expenses.sms.MessageParser.insertExpense
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SmsService : IntentService("com.nominalista.expenses.sms.SmsService") {
    var message: String? = null
    private val compositeDisposable = CompositeDisposable()

    companion object {
        fun getIntent(context: Context, message: String): Intent {
            val i = Intent(context, SmsService::class.java)
            i.putExtra("EXTRA", message)
            return i
        }
    }

    override fun onHandleIntent(intent: Intent) {
        intent.extras?.let {
            val messageBody: String = it["EXTRA"] as String
            message = messageBody
            val rule = getRule(applicationContext, messageBody)
            val defaultCurrency = applicationContext.application.preferenceDataSource.getDefaultCurrency(applicationContext.application)
            val localDataStore = applicationContext.application.localDataStore
            compositeDisposable += insertExpense(localDataStore, AndroidSchedulers.mainThread(), Schedulers.io(), defaultCurrency,rule, messageBody)
        }
    }


    private fun getRule(context: Context, message: String): Rule {
        val dataStore = context.application.localDataStore
        val rules = dataStore.getRules()
                .subscribeOn(Schedulers.io())
                .blockingGet()
        return rules.first { it.keywords.map { keyword -> message.contains(keyword, ignoreCase = true) }.fold(false) { acc, next -> acc || next } }
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
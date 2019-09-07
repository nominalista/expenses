package com.nominalista.expenses.currencyselection

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity

class CurrencySelectionActivity : BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_RIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_selection)
    }

    companion object {

        const val EXTRA_CURRENCY = "currency"

        fun start(fragment: Fragment, requestCode: Int) {
            val intent = Intent(fragment.requireContext(), CurrencySelectionActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)
        }
    }
}
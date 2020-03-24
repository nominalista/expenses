package com.nominalista.expenses.currencyselection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.util.extensions.afterTextChanged
import com.nominalista.expenses.util.extensions.hideKeyboard
import com.nominalista.expenses.util.extensions.showKeyboard
import kotlinx.android.synthetic.main.fragment_currency_selection.*

class CurrencySelectionFragment : Fragment(), CurrencyAdapter.Listener {

    private val adapter by lazy { CurrencyAdapter(this) }

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var runnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currency_selection, container, false)
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        watchEditText()
        showKeyboard()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        adapter.submitList(Currency.values().toList())
    }

    private fun watchEditText() {
        editText.afterTextChanged { dispatchCurrencyFilter() }
    }

    private fun dispatchCurrencyFilter() {
        handler.removeCallbacks(runnable)

        runnable = Runnable {
            val query = editText.text.toString()
            adapter.submitList(getFilteredCurrencies(query))
        }

        handler.postDelayed(runnable, QUERY_APPLIANCE_DELAY)
    }

    @SuppressLint("DefaultLocale")
    private fun getFilteredCurrencies(query: String): List<Currency> {
        val simplifiedQuery = query.toLowerCase()

        return Currency.values().filter { currency ->
            val code = currency.code.toLowerCase()
            val title = currency.title.toLowerCase()
            code.contains(simplifiedQuery) || title.contains(simplifiedQuery)
        }
    }

    private fun showKeyboard() {
        showKeyboard(editText, KEYBOARD_APPEARANCE_DELAY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }

    override fun onCurrencyClick(currency: Currency) {
        val data = Intent().apply {
            putExtra(CurrencySelectionActivity.EXTRA_CURRENCY, currency as Parcelable)
        }

        requireActivity().setResult(Activity.RESULT_OK, data)
        requireActivity().finish()
    }

    companion object {

        private const val QUERY_APPLIANCE_DELAY = 300L
        private const val KEYBOARD_APPEARANCE_DELAY = 300L
    }
}
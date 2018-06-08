package com.nominalista.expenses.userinterface.common.currencyselection

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency

class CurrencySelectionDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    companion object {

        fun newInstance() = CurrencySelectionDialogFragment()
    }

    var onCurrencySelected: ((Currency) -> Unit)? = null

    private val currencies = Currency.values()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setTitle(R.string.select_currency)
                .setItems(createItems(), this)
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .create()
    }

    private fun createItems(): Array<String> {
        return currencies
                .map { currency -> currency.description }
                .toTypedArray()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        onCurrencySelected?.invoke(currencies[which])
    }
}
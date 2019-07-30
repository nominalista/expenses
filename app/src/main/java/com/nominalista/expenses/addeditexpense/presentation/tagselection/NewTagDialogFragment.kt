package com.nominalista.expenses.addeditexpense.presentation.tagselection

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.util.extensions.afterTextChanged
import com.nominalista.expenses.util.extensions.hideKeyboard
import com.nominalista.expenses.util.extensions.showKeyboard


class NewTagDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = NewTagDialogFragment()
    }

    var tagCreated: ((Tag) -> Unit)? = null

    private lateinit var editText: EditText
    private val text get() = editText.text.toString()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setView(createView())
                .setPositiveButton(R.string.add) { _, _ -> tagCreated?.invoke(Tag(0, text)) }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setOnDismissListener { hideKeyboard() }
                .create()
    }

    @SuppressLint("InflateParams")
    private fun createView(): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_new_tag, null)
        bindViews(view)
        watchEditText()
        return view
    }

    private fun bindViews(view: View) {
        editText = view.findViewById(R.id.edit_text)
    }

    private fun watchEditText() {
        showKeyboard(editText)
        editText.afterTextChanged {
            val dialog = dialog as AlertDialog
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = text.isNotEmpty()
        }
    }
}
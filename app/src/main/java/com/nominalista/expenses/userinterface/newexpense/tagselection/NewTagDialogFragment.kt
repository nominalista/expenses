package com.nominalista.expenses.userinterface.newexpense.tagselection

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.afterTextChanged
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import io.reactivex.disposables.CompositeDisposable


class NewTagDialogFragment : DialogFragment() {

    companion object {

        fun newInstance() = NewTagDialogFragment()
    }

    private lateinit var editText: EditText

    private lateinit var model: NewTagDialogModel
    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setView(createView())
                .setPositiveButton(R.string.add, { _, _ -> model.addTag() })
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .create()
    }

    private fun createView(): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_new_tag, null)
        bindViews(view)
        watchEditText()
        setupViewModel()
        return view
    }

    private fun bindViews(view: View) {
        editText = view.findViewById(R.id.edit_text)
    }

    private fun watchEditText() {
        editText.afterTextChanged { model.updateName(it?.toString() ?: "") }
    }

    private fun setupViewModel() {
        val factory = NewTagDialogModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(NewTagDialogModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        subscribeModel()
    }

    private fun subscribeModel() {
        compositeDisposable += model.isAddEnabled
                .toObservable()
                .subscribe { isAddEnabled ->
                    val dialog = dialog as AlertDialog
                    val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    addButton.isEnabled = isAddEnabled
                }
    }

    // Lifecycle end

    override fun onPause() {
        super.onPause()
        unsubscribeModel()
    }

    private fun unsubscribeModel() {
        compositeDisposable.clear()
    }
}
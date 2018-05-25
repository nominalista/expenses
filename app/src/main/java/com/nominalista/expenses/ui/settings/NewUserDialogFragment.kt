package com.nominalista.expenses.ui.settings

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


class NewUserDialogFragment : DialogFragment() {

    companion object {

        fun newInstance() = NewUserDialogFragment()
    }

    private lateinit var editText: EditText

    private lateinit var viewModel: NewUserDialogModel

    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setView(createView())
                .setPositiveButton(R.string.ui_settings_add, { _, _ -> viewModel.addUser() })
                .setNegativeButton(R.string.ui_settings_cancel, { _, _ -> })
                .create()
    }

    private fun createView(): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_new_user, null)
        bindViews(view)
        watchEditText()
        setupViewModel()
        return view
    }

    private fun bindViews(view: View) {
        editText = view.findViewById(R.id.edit_text)
    }

    private fun watchEditText() {
        editText.afterTextChanged { editable -> viewModel.updateName(editable?.toString()) }
    }

    private fun setupViewModel() {
        val factory = NewUserDialogModel.Factory(requireContext().application)
        viewModel = ViewModelProviders.of(this, factory).get(NewUserDialogModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        subscribeViewModel()
    }

    private fun subscribeViewModel() {
        compositeDisposable += viewModel.isAddEnabled
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
        unsubscribeViewModel()
    }

    private fun unsubscribeViewModel() {
        compositeDisposable.clear()
    }
}
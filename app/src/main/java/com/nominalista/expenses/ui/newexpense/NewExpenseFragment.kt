package com.nominalista.expenses.ui.newexpense

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.*
import com.nominalista.expenses.model.User
import com.nominalista.expenses.ui.common.currencyselection.CurrencySelectionDialogFragment
import com.nominalista.expenses.ui.common.dateselection.DateSelectionDialogFragment
import com.nominalista.expenses.ui.common.userselection.UserSelectionDialogFragment
import io.reactivex.disposables.CompositeDisposable

class NewExpenseFragment : Fragment() {

    companion object {
        fun newInstance() = NewExpenseFragment()
    }

    private lateinit var containerLayout: View
    private lateinit var currencyText: TextView
    private lateinit var amountEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var userEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var notesEditText: EditText

    private lateinit var model: NewExpenseFragmentModel

    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_new_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        watchEditTexts()
        addListeners()
        setupViewModel()
        subscribeViewModel()
    }

    private fun bindWidgets(view: View) {
        containerLayout = view.findViewById(R.id.layout_container)
        currencyText = view.findViewById(R.id.text_symbol)
        amountEditText = view.findViewById(R.id.edit_text_amount)
        titleEditText = view.findViewById(R.id.edit_text_title)
        userEditText = view.findViewById(R.id.edit_text_user)
        dateEditText = view.findViewById(R.id.edit_text_date)
        notesEditText = view.findViewById(R.id.edit_text_notes)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_light_active_24dp)
        setHasOptionsMenu(true)
    }

    private fun watchEditTexts() {
        amountEditText.afterTextChanged { editable ->
            model.updateAmount(editable.toString().toFloatOrNull() ?: 0f)
        }
        titleEditText.afterTextChanged { editable ->
            model.updateTitle(editable.toString())
        }
        notesEditText.afterTextChanged { editable ->
            model.updateNotes(editable.toString())
        }
    }

    private fun addListeners() {
        currencyText.setOnClickListener { showCurrencySelection() }
        userEditText.setOnClickListener { showUserSelection { model.selectUser(it) } }
        dateEditText.setOnClickListener { showDateSelection() }
    }

    private fun showCurrencySelection() {
        val dialogFragment = CurrencySelectionDialogFragment.newInstance()
        dialogFragment.onCurrencySelected = { currency -> model.selectCurrency(currency) }
        dialogFragment.show(requireFragmentManager(), "CurrencySelectionDialogFragment")
    }

    private fun showUserSelection(listener: (User) -> Unit) {
        if (model.isUserSelectionEnabled) {
            val fragment = UserSelectionDialogFragment.newInstance(model.users)
            fragment.onUserSelected = listener
            fragment.show(requireFragmentManager(), "UserSelectionDialogFragment")
        } else {
            Snackbar.make(containerLayout,
                    R.string.ui_new_expense_no_saved_users,
                    Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    private fun showDateSelection() {
        val dialogFragment = DateSelectionDialogFragment()
        dialogFragment.onDateSelected = { date -> model.selectDate(date) }
        dialogFragment.show(requireFragmentManager(), "DateSelectionDialogFragment")
    }

    private fun setupViewModel() {
        val factory = NewExpenseFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(NewExpenseFragmentModel::class.java)
    }

    private fun subscribeViewModel() {
        compositeDisposable += model.selectedCurrency
                .toObservable()
                .subscribe { selectedCurrency ->
                    val context = requireContext()
                    val text = context.getString(R.string.ui_new_expense_currency,
                            selectedCurrency.flag,
                            selectedCurrency.code)
                    currencyText.text = text
                }
        compositeDisposable += model.selectedUser
                .toObservable()
                .subscribe { selectedBuyer ->
                    userEditText.setText(selectedBuyer.name)
                }
        compositeDisposable += model.selectedDate
                .toObservable()
                .subscribe { selectedDate ->
                    dateEditText.setText(selectedDate.toString("dd-MM-yyyy"))
                }
        compositeDisposable += model.finish
                .toObservable()
                .subscribe { requireActivity().onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        showKeyboard(amountEditText)
    }

    // Lifecycle end

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeViewModel()
    }

    private fun unsubscribeViewModel() {
        compositeDisposable.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_new_expense, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> backSelected()
            R.id.save -> saveSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    private fun saveSelected(): Boolean {
        model.createExpense()
        return true
    }
}

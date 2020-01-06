package com.nominalista.expenses.addeditexpense.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.nominalista.expenses.R
import com.nominalista.expenses.addeditexpense.presentation.dateselection.DateSelectionDialogFragment
import com.nominalista.expenses.currencyselection.CurrencySelectionActivity
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.util.READABLE_DATE_FORMAT
import com.nominalista.expenses.util.extensions.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add_edit_expense.*
import org.threeten.bp.LocalDate

class AddEditExpenseFragment : Fragment() {

    private lateinit var activityModel: AddEditExpenseActivityModel
    private lateinit var model: AddEditExpenseFragmentModel

    private val disposables = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_edit_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        watchEditTexts()
        addListeners()
        initializeModels()
        bindModels()
        showKeyboard()
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp)
        setHasOptionsMenu(true)
    }

    private fun watchEditTexts() {
        editTextAmount.afterTextChanged {
            model.updateAmount(it.toString().toDoubleOrNull() ?: 0.0)
        }
        editTextTitle.afterTextChanged { model.updateTitle(it.toString()) }
        editTextNotes.afterTextChanged { model.updateNotes(it.toString()) }
    }

    private fun addListeners() {
        textSymbol.setOnClickListener { showCurrencySelection() }
        containerTags.setOnClickListener { showTagSelection() }
        textDate.setOnClickListener { showDateSelection() }
    }

    private fun showCurrencySelection() {
        CurrencySelectionActivity.start(this, REQUEST_CODE_SELECT_CURRENCY)
    }

    private fun showTagSelection() {
        NavHostFragment.findNavController(this).navigate(R.id.action_tag_selection)
    }

    private fun showDateSelection() {
        DateSelectionDialogFragment.newInstance().apply {
            dateSelected = { y, m, d -> model.selectDate(y, m, d) }
        }.show(requireFragmentManager(), DateSelectionDialogFragment.TAG)
    }

    private fun initializeModels() {
        activityModel = ViewModelProviders.of(requireActivity())
            .get(AddEditExpenseActivityModel::class.java)

        val args = arguments?.let { AddEditExpenseFragmentArgs.fromBundle(it) }
        val factory =
            AddEditExpenseFragmentModel.Factory(requireContext().application, args?.expense)
        model = ViewModelProviders.of(this, factory).get(AddEditExpenseFragmentModel::class.java)
    }

    private fun bindModels() {
        activityModel.selectedTags.observe(this, Observer { model.selectTags(it) })

        disposables += model.selectedCurrency
            .toObservable()
            .subscribe { updateCurrencyText(it) }
        disposables += model.selectedDate.toObservable().subscribe { updateDateText(it) }
        disposables += model.selectedTags.toObservable().subscribe { updateTagLayout(it) }
        disposables += model.finish.toObservable().subscribe { finish() }

        editTextAmount.setText(makeEasilyEditableAmount(model.amount))
        editTextTitle.setText(model.title)
        editTextNotes.setText(model.notes)
    }

    private fun makeEasilyEditableAmount(amount: Double?): String {
        return when {
            amount == null -> ""
            amount - amount.toInt().toDouble() == 0.0 -> amount.toInt().toString()
            else -> amount.toString()
        }
    }

    private fun updateCurrencyText(currency: Currency) {
        val context = requireContext()
        val text = context.getString(
            R.string.currency_abbreviation,
            currency.flag,
            currency.code
        )
        textSymbol.text = text
    }

    private fun updateDateText(date: LocalDate) {
        textDate.text = date.toString(READABLE_DATE_FORMAT)
    }

    private fun updateTagLayout(tags: List<Tag>) {
        updateSelectTagsText(tags.isEmpty())
        updateChipGroup(tags)
    }

    private fun updateSelectTagsText(isVisible: Boolean) {
        text_select_tags.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateChipGroup(tags: List<Tag>) {
        chip_group.removeAllViews()
        tags.forEach { chip_group.addView(createChip(it.name)) }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.text = text
        chip.isClickable = false
        return chip
    }

    private fun finish() {
        requireActivity().onBackPressed()
    }

    private fun showKeyboard() {
        showKeyboard(editTextAmount, KEYBOARD_APPEARANCE_DELAY)
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        clearDisposables()
        hideKeyboard()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_expense, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        model.saveExpense()
        return true
    }

    // Results

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when(requestCode) {
            REQUEST_CODE_SELECT_CURRENCY -> {
                val currency: Currency? =
                    data?.getParcelableExtra(CurrencySelectionActivity.EXTRA_CURRENCY)
                currency?.let { model.selectCurrency(it) }
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_SELECT_CURRENCY = 1

        private const val KEYBOARD_APPEARANCE_DELAY = 300L
    }
}

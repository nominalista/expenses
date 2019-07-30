package com.nominalista.expenses.addeditexpense.presentation

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.addeditexpense.presentation.dateselection.DateSelectionDialogFragment
import com.nominalista.expenses.addeditexpense.presentation.timeselection.TimeSelectionDialogFragment
import com.nominalista.expenses.common.presentation.currencyselection.CurrencySelectionDialogFragment
import com.nominalista.expenses.util.extensions.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add_edit_expense.*

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
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_light_active_24dp)
        setHasOptionsMenu(true)
    }

    private fun watchEditTexts() {
        showKeyboard(edit_text_amount, 200)
        edit_text_amount.afterTextChanged {
            model.updateAmount(it.toString().toFloatOrNull() ?: 0f)
        }
        edit_text_title.afterTextChanged { model.updateTitle(it.toString()) }
        edit_text_notes.afterTextChanged { model.updateNotes(it.toString()) }
    }

    private fun addListeners() {
        text_symbol.setOnClickListener { showCurrencySelection() }
        layout_tag.setOnClickListener { showTagSelection() }
        text_date.setOnClickListener { showDateSelection() }
    }

    private fun showCurrencySelection() {
        val dialogFragment = CurrencySelectionDialogFragment.newInstance()
        dialogFragment.onCurrencySelected = { currency -> model.selectCurrency(currency) }
        dialogFragment.show(requireFragmentManager(), "CurrencySelectionDialogFragment")
    }

    private fun showTagSelection() {
        NavHostFragment.findNavController(this).navigate(R.id.action_tag_selection)
    }

    private fun showDateSelection() {
        val dialogFragment = DateSelectionDialogFragment()
        dialogFragment.dateSelected = { y, m, d -> showTimeSelection(y, m, d) }
        dialogFragment.show(requireFragmentManager(), "DateSelectionDialogFragment")
    }

    private fun showTimeSelection(year: Int, month: Int, day: Int) {
        val dialogFragment = TimeSelectionDialogFragment()
        dialogFragment.timeSelected = { h, m -> selectDate(year, month, day, h, m) }
        dialogFragment.show(requireFragmentManager(), "TimeSelectionDialogFragment")
    }

    private fun selectDate(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        model.selectDate(year, month, day, hour, minute)
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

        edit_text_amount.setText(makeEasilyEditableAmount(model.amount))
        edit_text_title.setText(model.title)
        edit_text_notes.setText(model.notes)
    }

    private fun makeEasilyEditableAmount(amount: Float?): String {
        return when {
            amount == null -> ""
            amount - amount.toInt().toFloat() == 0f -> amount.toInt().toString()
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
        text_symbol.text = text
    }

    private fun updateDateText(date: Date) {
        text_date.text = date.toReadableString()
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
        model.saveExpense()
        return true
    }
}

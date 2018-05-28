package com.nominalista.expenses.ui.newexpense

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.extensions.*
import com.nominalista.expenses.ui.MainActivity
import com.nominalista.expenses.ui.common.currencyselection.CurrencySelectionDialogFragment
import com.nominalista.expenses.ui.common.dateselection.DateSelectionDialogFragment
import com.nominalista.expenses.ui.common.timeselection.TimeSelectionDialogFragment
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class NewExpenseFragment : Fragment() {

    companion object {
        fun newInstance() = NewExpenseFragment()
    }

    private lateinit var currencyText: TextView
    private lateinit var amountEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var tagLayout: ViewGroup
    private lateinit var selectTagsText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var dateText: TextView

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
        currencyText = view.findViewById(R.id.text_symbol)
        amountEditText = view.findViewById(R.id.edit_text_amount)
        titleEditText = view.findViewById(R.id.edit_text_title)
        notesEditText = view.findViewById(R.id.edit_text_notes)
        tagLayout = view.findViewById(R.id.layout_tag)
        selectTagsText = view.findViewById(R.id.text_select_tags)
        chipGroup = view.findViewById(R.id.chip_group)
        dateText = view.findViewById(R.id.text_date)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_light_active_24dp)
        setHasOptionsMenu(true)
    }

    private fun watchEditTexts() {
        amountEditText.afterTextChanged { model.updateAmount(it.toString().toFloatOrNull() ?: 0f) }
        titleEditText.afterTextChanged { model.updateTitle(it.toString()) }
        notesEditText.afterTextChanged { model.updateNotes(it.toString()) }
    }

    private fun addListeners() {
        currencyText.setOnClickListener { showCurrencySelection() }
        tagLayout.setOnClickListener { showTagSelection() }
        dateText.setOnClickListener { showDateSelection() }
    }

    private fun showCurrencySelection() {
        val dialogFragment = CurrencySelectionDialogFragment.newInstance()
        dialogFragment.onCurrencySelected = { currency -> model.selectCurrency(currency) }
        dialogFragment.show(requireFragmentManager(), "CurrencySelectionDialogFragment")
    }

    private fun showTagSelection() {
        (requireActivity() as MainActivity).navigateToTagSelection { model.selectTags(it) }
    }

    private fun showDateSelection() {
        val dialogFragment = DateSelectionDialogFragment()
        dialogFragment.dateSelected = { y, m, d -> showTimeSelection(y, m, d) }
        dialogFragment.show(requireFragmentManager(), "DateSelectionDialogFragment")
    }

    private fun showTimeSelection(year: Int, month: Int, dayOfMonth: Int) {
        val dialogFragment = TimeSelectionDialogFragment()
        dialogFragment.timeSelected = { h, m -> selectDate(year, month, dayOfMonth, h, m)}
        dialogFragment.show(requireFragmentManager(), "TimeSelectionDialogFragment")
    }

    private fun selectDate(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int) {
        model.selectDate(year, month, dayOfMonth, hourOfDay, minute)
    }

    private fun setupViewModel() {
        val factory = NewExpenseFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(NewExpenseFragmentModel::class.java)
    }

    private fun subscribeViewModel() {
        compositeDisposable += model.selectedCurrency
                .toObservable()
                .subscribe { updateCurrencyText(it) }
        compositeDisposable += model.selectedDate.toObservable().subscribe { updateDateText(it) }
        compositeDisposable += model.selectedTags.toObservable().subscribe { updateTagLayout(it) }
        compositeDisposable += model.finish.toObservable().subscribe { finish() }
    }

    private fun updateCurrencyText(currency: Currency) {
        val context = requireContext()
        val text = context.getString(R.string.currency_abbreviation,
                currency.flag,
                currency.code)
        currencyText.text = text
    }

    private fun updateDateText(date: Date) {
        dateText.text = date.toReadableString(requireContext())
    }

    private fun updateTagLayout(tags: List<Tag>) {
        updateSelectTagsText(tags.isEmpty())
        updateChipGroup(tags)
    }

    private fun updateSelectTagsText(isVisible: Boolean) {
        selectTagsText.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun updateChipGroup(tags: List<Tag>) {
        chipGroup.removeAllViews()
        tags.forEach { chipGroup.addView(createChip(it.name)) }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.chipText = text
        chip.isClickable = false
        return chip
    }

    private fun finish() {
        requireActivity().onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        // showKeyboard(amountEditText)
    }

    // Lifecycle end

    override fun onPause() {
        super.onPause()
        // hideKeyboard()
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

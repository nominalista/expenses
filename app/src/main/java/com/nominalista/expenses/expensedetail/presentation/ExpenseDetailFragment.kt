package com.nominalista.expenses.expensedetail.presentation

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.addeditexpense.presentation.AddEditExpenseActivity
import io.reactivex.disposables.CompositeDisposable

class ExpenseDetailFragment : Fragment() {

    private lateinit var amountText: TextView
    private lateinit var currencyText: TextView
    private lateinit var titleText: TextView
    private lateinit var noTagsText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var dateText: TextView
    private lateinit var notesText: TextView

    private lateinit var model: ExpenseDetailFragmentModel
    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_expense_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupModel()
        subscribeModel()
    }

    private fun bindWidgets(view: View) {
        amountText = view.findViewById(R.id.text_amount)
        currencyText = view.findViewById(R.id.text_currency)
        titleText = view.findViewById(R.id.text_title)
        noTagsText = view.findViewById(R.id.text_no_tags)
        chipGroup = view.findViewById(R.id.chip_group)
        dateText = view.findViewById(R.id.text_date)
        notesText = view.findViewById(R.id.text_notes)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.setTitle(R.string.expense_detail_title)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupModel() {
        val args = arguments?.let { ExpenseDetailFragmentArgs.fromBundle(it) } ?: return
        val factory = ExpenseDetailFragmentModel.Factory(requireContext().application, args.expense)
        model = ViewModelProviders.of(this, factory).get(ExpenseDetailFragmentModel::class.java)
    }

    private fun subscribeModel() {
        compositeDisposable += model.amount.toObservable().subscribe { amountText.text = it }
        compositeDisposable += model.currency.toObservable().subscribe { currencyText.text = it }
        compositeDisposable += model.title.toObservable().subscribe { titleText.text = it }
        compositeDisposable += model.date.toObservable().subscribe { dateText.text = it }
        compositeDisposable += model.notes.toObservable().subscribe { notesText.text = it }
        compositeDisposable += model.tags
            .toObservable()
            .subscribe { configureChipGroup(it); configureNoTagsText(it.isEmpty()) }
        compositeDisposable += model.showEdit
            .toObservable()
            .subscribe { AddEditExpenseActivity.start(requireContext(), it) }
        compositeDisposable += model.finish
            .toObservable()
            .subscribe { requireActivity().onBackPressed() }
    }

    private fun configureChipGroup(tags: List<Tag>) {
        chipGroup.removeAllViews()
        tags.forEach { chipGroup.addView(createChip(it.name)) }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.text = text
        chip.isClickable = false
        return chip
    }

    private fun configureNoTagsText(isVisible: Boolean) {
        noTagsText.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeModel()
    }

    private fun unsubscribeModel() {
        currencyText.text = ""
        amountText.text = ""
        titleText.text = ""
        dateText.text = ""
        notesText.text = ""

        chipGroup.removeAllViews()
        noTagsText.isVisible = false

        compositeDisposable.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_expense_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            R.id.edit -> editSelected()
            R.id.delete -> deleteSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    private fun editSelected(): Boolean {
        model.edit()
        return true
    }

    private fun deleteSelected(): Boolean {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_expense_message)
            .setPositiveButton(R.string.yes) { _, _ -> model.delete() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
        return true
    }
}
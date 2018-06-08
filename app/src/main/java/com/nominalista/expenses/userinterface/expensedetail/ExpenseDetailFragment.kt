package com.nominalista.expenses.userinterface.expensedetail

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.userinterface.expensedetail.ExpenseDetailActivity.Companion.EXTRA_EXPENSE
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
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_active_light_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupModel() {
        val extras = requireActivity().intent?.extras ?: return
        val expense = extras.getParcelable<Expense>(EXTRA_EXPENSE)
        val factory = ExpenseDetailFragmentModel.Factory(requireContext().application, expense)
        model = ViewModelProviders.of(this, factory).get(ExpenseDetailFragmentModel::class.java)
    }

    private fun subscribeModel() {
        currencyText.text = model.currency
        amountText.text = model.amount
        titleText.text = model.title
        dateText.text = model.date
        notesText.text = model.notes

        setupChipGroup()
        setupNoTagsText()

        compositeDisposable += model.finish
                .toObservable()
                .subscribe { requireActivity().onBackPressed() }
    }

    private fun setupChipGroup() {
        model.tags.forEach { chipGroup.addView(createChip(it.name)) }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.chipText = text
        chip.isClickable = false
        return chip
    }

    private fun setupNoTagsText() {
        val isVisible = model.tags.isEmpty()
        noTagsText.visibility = if (isVisible) View.VISIBLE else View.GONE
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
        noTagsText.visibility = View.VISIBLE

        compositeDisposable.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_expense_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> backSelected()
            R.id.delete -> deleteSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    private fun deleteSelected(): Boolean {
        model.delete()
        return true
    }
}
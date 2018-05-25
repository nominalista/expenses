package com.nominalista.expenses.ui.expensedetail

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.model.Expense
import io.reactivex.disposables.CompositeDisposable

private const val ARGUMENT_EXPENSE = "com.nominalista.expense.ARGUMENT_EXPENSE"

class ExpenseDetailFragment : Fragment() {

    companion object {

        fun newInstance(expense: Expense): ExpenseDetailFragment {
            val bundle = Bundle()
            bundle.putParcelable(ARGUMENT_EXPENSE, expense)
            val fragment = ExpenseDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var amountText: TextView
    private lateinit var currencyText: TextView
    private lateinit var titleText: TextView
    private lateinit var userText: TextView
    private lateinit var dateText: TextView
    private lateinit var notesText: TextView

    private lateinit var model: ExpenseDetailFragmentModel

    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
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
        userText = view.findViewById(R.id.text_user)
        dateText = view.findViewById(R.id.text_date)
        notesText = view.findViewById(R.id.text_notes)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.setTitle(R.string.ui_expense_detail_details)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_active_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupModel() {
        val arguments = arguments ?: return
        val expense = arguments.getParcelable<Expense>(ARGUMENT_EXPENSE)
        val factory = ExpenseDetailFragmentModel.Factory(requireContext().application, expense)
        model = ViewModelProviders.of(this, factory).get(ExpenseDetailFragmentModel::class.java)
    }

    private fun subscribeModel() {
        currencyText.text = model.currency
        amountText.text = model.amount
        titleText.text = model.title
        userText.text = model.user
        dateText.text = model.date
        notesText.text = model.notes

        compositeDisposable += model.finish
                .toObservable()
                .subscribe { requireActivity().onBackPressed() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeModel()
    }

    private fun unsubscribeModel() {
        currencyText.text = ""
        amountText.text = ""
        titleText.text = ""
        userText.text = ""
        dateText.text = ""
        notesText.text = ""

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
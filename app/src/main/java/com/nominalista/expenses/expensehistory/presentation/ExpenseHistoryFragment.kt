package com.nominalista.expenses.expensehistory.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.expensedetail.presentation.ExpenseDetailActivity
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import io.reactivex.disposables.CompositeDisposable
import org.threeten.bp.LocalDate

class ExpenseHistoryFragment : Fragment(), CalendarView.OnDateChangeListener {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var historyCalenderView: CalendarView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: ExpenseHistoryAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var model: ExpenseFragmentModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_expense_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupCalendarView()
        setupRecyclerView()
        setupViewModel()
        bindModel()
    }

    private fun bindWidgets(view: View) {
        expenseRecyclerView = view.findViewById(R.id.rvExpenseHistory)
        historyCalenderView = view.findViewById(R.id.cvExpenseHistory)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupViewModel() {
        val factory = ExpenseFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(ExpenseFragmentModel::class.java)
    }

    private fun setupCalendarView() {
        historyCalenderView.setOnDateChangeListener(this)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseHistoryAdapter()
        layoutManager = LinearLayoutManager(context)
        expenseRecyclerView.adapter = adapter
        expenseRecyclerView.layoutManager = layoutManager
    }

    private fun bindModel() {
        compositeDisposable += model.expenseItemModels
                .subscribe { adapter.submitList(it) }
        compositeDisposable += model.isLoading
                .subscribe { configureProgressBar(it) }
        compositeDisposable += model.showExpenseDetail
                .subscribe { showExpenseDetail(it) }
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.setTitle(R.string.expense_history_title)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindFromModel()
    }

    private fun unbindFromModel() {
        compositeDisposable.clear()
    }

    private fun configureProgressBar(isVisible: Boolean) {
        progressBar.isVisible = isVisible
    }

    private fun showExpenseDetail(expense: Expense) {
        ExpenseDetailActivity.start(requireContext(), expense)
    }

    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
        Toast.makeText(activity, "Selected date is $dayOfMonth", Toast.LENGTH_SHORT).show()
        model.showExpensesForTheDay(LocalDate.of(year, month + 1, dayOfMonth))
    }
}
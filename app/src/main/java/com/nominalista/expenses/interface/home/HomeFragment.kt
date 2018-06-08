package com.nominalista.expenses.`interface`.home

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.`interface`.MainActivity
import io.reactivex.disposables.CompositeDisposable

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var newExpenseButton: Button

    private lateinit var model: HomeFragmentModel
    private lateinit var adapter: HomeAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupRecyclerView()
        setupNewExpenseLayout()
        setupViewModel()
        subscribeViewModel()
    }

    private fun bindWidgets(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        newExpenseButton = view.findViewById(R.id.button_new_expense)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.setTitle(R.string.app_name)
        actionBar.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView() {
        adapter = HomeAdapter()
        layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun setupNewExpenseLayout() {
        newExpenseButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToNewExpense()
        }
    }

    private fun setupViewModel() {
        val factory = HomeFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(HomeFragmentModel::class.java)
    }

    private fun subscribeViewModel() {
        compositeDisposable += model.itemModels.toObservable().subscribe(adapter::submitList)
        compositeDisposable += model.showExpenseDetail
                .toObservable()
                .subscribe { showExpenseDetail(it) }
    }

    private fun showExpenseDetail(expense: Expense) {
        (requireActivity() as MainActivity).navigateToExpenseDetail(expense)
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        unbindFromViewModel()
    }

    private fun unbindFromViewModel() {
        compositeDisposable.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.item_settings -> settingsSelected()
            R.id.item_filter -> filterSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun settingsSelected(): Boolean {
        (requireActivity() as MainActivity).navigateToSettings()
        return true
    }

    private fun filterSelected(): Boolean {
        showTagFiltering()
        return true
    }

    private fun showTagFiltering() {
        val dialogFragment = TagFilteringDialogFragment.newInstance(model.tags.value)
        dialogFragment.tagsFiltered = { model.tagsFiltered(it) }
        dialogFragment.show(requireFragmentManager(), "TagFilteringDialogFragment")
    }
}

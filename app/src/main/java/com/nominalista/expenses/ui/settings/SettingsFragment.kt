package com.nominalista.expenses.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.ui.common.currencyselection.CurrencySelectionDialogFragment
import io.reactivex.disposables.CompositeDisposable
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar


class SettingsFragment : Fragment() {

    companion object {

        fun newInstance() = SettingsFragment()
    }

    private lateinit var containerLayout: ViewGroup
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: SettingsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var model: SettingsFragmentModel

    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupRecyclerView()
        setupViewModel()
        subscribeViewModel()
    }

    private fun bindWidgets(view: View) {
        containerLayout = view.findViewById(R.id.layout_container)
        recyclerView = view.findViewById(R.id.recycler_view)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.setTitle(R.string.settings)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_active_light_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView() {
        adapter = SettingsAdapter()
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun setupViewModel() {
        val factory = SettingsFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(SettingsFragmentModel::class.java)
    }

    private fun subscribeViewModel() {
        compositeDisposable += model.itemModels
                .toObservable()
                .subscribe(adapter::submitList)
        compositeDisposable += model.showCurrencySelectionDialog
                .toObservable()
                .subscribe { showCurrencySelectionDialog() }
        compositeDisposable += model.showDeleteAllExpensesDialog
                .toObservable()
                .subscribe { showDeleteAllExpensesDialog() }
        compositeDisposable += model.showAllExpensesDeletedMessage
                .toObservable()
                .subscribe { showAllExpensesDeletedMessage() }
    }

    private fun showCurrencySelectionDialog() {
        val dialogFragment = CurrencySelectionDialogFragment.newInstance()
        dialogFragment.onCurrencySelected = { currency -> model.updateDefaultCurrency(currency) }
        dialogFragment.show(requireFragmentManager(), "CurrencySelectionDialogFragment")
    }

    private fun showDeleteAllExpensesDialog() {
        AlertDialog.Builder(requireActivity())
                .setMessage(R.string.delete_all_expenses_message)
                .setPositiveButton(R.string.ok, { _, _ -> model.deleteAllExpenses() })
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .create()
                .show()
    }

    private fun showAllExpensesDeletedMessage() {
        val snackbar = Snackbar.make(containerLayout,
                R.string.all_expenses_deleted_message,
                Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeViewModel()
    }

    private fun unsubscribeViewModel() {
        compositeDisposable.clear()
    }

    // Options

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> backSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }
}
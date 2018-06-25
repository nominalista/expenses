package com.nominalista.expenses.userinterface.settings

import android.Manifest
import android.net.Uri
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
import com.nominalista.expenses.userinterface.common.currencyselection.CurrencySelectionDialogFragment
import io.reactivex.disposables.CompositeDisposable
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.nominalista.expenses.infrastructure.utils.isGranted
import com.nominalista.expenses.infrastructure.utils.isPermissionGranted

class SettingsFragment : Fragment() {

    private lateinit var containerLayout: ViewGroup
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: SettingsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var model: SettingsFragmentModel

    private val compositeDisposable = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupRecyclerView()
        setupModel()
        bindModel()
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

    private fun setupModel() {
        val factory = SettingsFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(SettingsFragmentModel::class.java)
    }

    private fun bindModel() {
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
        compositeDisposable += model.showExpenseExportMessage
                .toObservable()
                .subscribe { showExpenseExportMessage(it) }
        compositeDisposable += model.showWebsite
                .toObservable()
                .subscribe { showWebsite(it) }
        compositeDisposable += model.requestWriteExternalStoragePermission
                .toObservable()
                .subscribe { requestWriteExternalStoragePermission(it) }
    }

    private fun showCurrencySelectionDialog() {
        val dialogFragment = CurrencySelectionDialogFragment.newInstance()
        dialogFragment.onCurrencySelected = { currency -> model.updateDefaultCurrency(currency) }
        dialogFragment.show(requireFragmentManager(), "CurrencySelectionDialogFragment")
    }

    private fun showDeleteAllExpensesDialog() {
        AlertDialog.Builder(requireActivity())
                .setMessage(R.string.delete_all_expenses_message)
                .setPositiveButton(R.string.yes) { _, _ -> model.deleteAllExpenses() }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
    }

    private fun showAllExpensesDeletedMessage() {
        val snackbar = Snackbar.make(containerLayout,
                R.string.all_expenses_deleted,
                Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    private fun showExpenseExportMessage(message: String) {
        Snackbar.make(containerLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showWebsite(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(browserIntent)
    }

    private fun requestWriteExternalStoragePermission(requestCode: Int) {
        if (isPermissionGranted(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            model.permissionGranted(requestCode)
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        }
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        unbindModel()
    }

    private fun unbindModel() {
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

    // Requests

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (isGranted(grantResults)) model.permissionGranted(requestCode)
    }
}
package com.nominalista.expenses.settings.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nominalista.expenses.R
import com.nominalista.expenses.currencyselection.CurrencySelectionActivity
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.onboarding.OnboardingActivity
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.extensions.startActivitySafely
import com.nominalista.expenses.util.isGranted
import com.nominalista.expenses.util.isPermissionGranted
import io.reactivex.disposables.CompositeDisposable
import java.util.*

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
        compositeDisposable += model.selectDefaultCurrency
            .toObservable()
            .subscribe { selectDefaultCurrency() }
        compositeDisposable += model.selectFileForImport
            .toObservable()
            .subscribe { selectFileForImport() }
        compositeDisposable += model.showDeleteAllExpensesDialog
            .toObservable()
            .subscribe { showDeleteAllExpensesDialog() }
        compositeDisposable += model.requestWriteExternalStorageForExport
            .toObservable()
            .subscribe { requestWriteExternalStorageForExport() }
        compositeDisposable += model.showExpenseImportFailureDialog
            .toObservable()
            .subscribe { showExpenseImportFailureDialog() }
        compositeDisposable += model.showExpenseExportFailureDialog
            .toObservable()
            .subscribe { showExpenseExportFailureDialog() }
        compositeDisposable += model.showMessage
            .toObservable()
            .subscribe { showMessage(it) }
        compositeDisposable += model.showActivity
            .toObservable()
            .subscribe { showActivity(it) }
        compositeDisposable += model.navigateToOnboarding
            .toObservable()
            .subscribe { navigateToOnboarding() }

        compositeDisposable += model.observeWorkInfo
            .toObservable()
            .subscribe { observeWorkInfo(it) }
    }

    private fun selectDefaultCurrency() {
        CurrencySelectionActivity.start(this, REQUEST_CODE_SELECT_DEFAULT_CURRENCY)
    }

    private fun selectFileForImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(XLS_MIME_TYPE))
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE_FOR_IMPORT)
    }

    private fun requestWriteExternalStorageForExport() {
        if (isPermissionGranted(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            model.permissionGranted()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE_FOR_EXPORT
            )
        }
    }

    private fun showDeleteAllExpensesDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(R.string.delete_all_expenses_message)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAllExpenses() }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun showExpenseImportFailureDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(R.string.expense_import_failure_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setNeutralButton(R.string.download_template) { _, _ -> model.downloadTemplate() }
            .create()
            .show()
    }

    private fun showExpenseExportFailureDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(R.string.expense_export_failure_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .create()
            .show()
    }

    private fun showMessage(messageId: Int) {
        Snackbar.make(containerLayout, messageId, Snackbar.LENGTH_LONG).show()
    }

    private fun showActivity(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireActivity().startActivitySafely(intent)
    }

    private fun navigateToOnboarding() {
        OnboardingActivity.start(requireContext())
    }

    private fun observeWorkInfo(id: UUID) {
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(id)
            .observe(this, Observer { model.handleWorkInfo(it) })
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

    // Results

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CODE_SELECT_DEFAULT_CURRENCY -> {
                val currency: Currency? =
                    data?.getParcelableExtra(CurrencySelectionActivity.EXTRA_CURRENCY)
                currency?.let { model.defaultCurrencySelect(it) }
            }
            REQUEST_CODE_SELECT_FILE_FOR_IMPORT -> {
                val uriToSelectedFile = data?.data
                uriToSelectedFile?.let { model.fileForImportSelected(it) }
            }
        }
    }

    // Permissions

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (isGranted(grantResults)) model.permissionGranted()
    }

    companion object {

        private const val REQUEST_CODE_SELECT_DEFAULT_CURRENCY = 1
        private const val REQUEST_CODE_SELECT_FILE_FOR_IMPORT = 2
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_FOR_EXPORT = 3

        private const val XLS_MIME_TYPE = "application/vnd.ms-excel"
    }
}
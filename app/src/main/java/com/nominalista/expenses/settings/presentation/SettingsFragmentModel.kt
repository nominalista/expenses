package com.nominalista.expenses.settings.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import com.nominalista.expenses.settings.domain.DeleteAllExpensesUseCase
import com.nominalista.expenses.settings.domain.ExportExpensesUseCase
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io

class SettingsFragmentModel(
    application: Application,
    private val preferenceDataSource: PreferenceDataSource,
    private val exportExpensesUseCase: ExportExpensesUseCase,
    private val deleteAllExpensesUseCase: DeleteAllExpensesUseCase
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())

    val showCurrencySelectionDialog = Event()
    val showDeleteAllExpensesDialog = Event()
    val showAllExpensesDeletedMessage = Event()
    val showExpenseExportMessage = DataEvent<String>()
    val showWebsite = DataEvent<Uri>()
    val requestWriteExternalStoragePermission =
        DataEvent<Int>()

    private var isExporting = false
    private var isDeleting = false

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        itemModels.value = createExpenseSection() + createGeneralSection()
    }

    // Expense section

    private fun createExpenseSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()

        itemModels += createExpenseHeader(context)
        itemModels += createDefaultCurrency(context)
        itemModels += createExportExpenses(context)
        itemModels += createDeleteAllExpenses(context)

        return itemModels
    }

    private fun createExpenseHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.expenses))

    private fun createDefaultCurrency(context: Context): SettingItemModel {
        val defaultCurrency = preferenceDataSource.getDefaultCurrency(context)

        val title = context.getString(R.string.default_currency)

        val summary = context.getString(
            R.string.default_currency_summary,
            defaultCurrency.flag,
            defaultCurrency.title,
            defaultCurrency.code
        )

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showCurrencySelectionDialog.next() }
        }
    }

    private fun createExportExpenses(context: Context): SettingItemModel {
        val title = context.getString(R.string.export_to_excel)

        return ActionSettingItemModel(title).apply {
            click = {
                requestWriteExternalStoragePermission.next(REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun createDeleteAllExpenses(context: Context): SettingItemModel {
        val title = context.getString(R.string.delete_all)

        return ActionSettingItemModel(title).apply {
            click = { showDeleteAllExpensesDialog.next() }
        }
    }

    // General section

    private fun createGeneralSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()

        itemModels += createGeneralHeader(context)
        itemModels += createViewSourceCode(context)

        return itemModels
    }

    private fun createGeneralHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.general))

    private fun createViewSourceCode(context: Context): SettingItemModel {
        val title = context.getString(R.string.view_source_code)

        return ActionSettingItemModel(title).apply {
            click = { showWebsite.next(GITHUB_URI) }
        }
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    // Public

    fun updateDefaultCurrency(defaultCurrency: Currency) {
        getApplication<Application>().let {
            preferenceDataSource.setDefaultCurrency(it, defaultCurrency)
        }

        loadItemModels()
    }

    fun permissionGranted(requestCode: Int) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) exportExpenses()
    }

    private fun exportExpenses() {
        if (isExporting) return

        disposables += exportExpensesUseCase(getApplication<Application>())
            .subscribeOn(io())
            .observeOn(mainThread())
            .doOnSubscribe { isExporting = true }
            .doFinally { isExporting = false }
            .subscribe({
                showExpenseExportMessage(true)
            }, {
                showExpenseExportMessage(false)
            })
    }

    private fun showExpenseExportMessage(isSuccessful: Boolean) {
        val messageResId = if (isSuccessful) {
            R.string.expense_export_success_message
        } else {
            R.string.expense_export_failure_message
        }

        showExpenseExportMessage.next(getApplication<Application>().getString(messageResId))
    }

    fun deleteAllExpenses() {
        disposables += deleteAllExpensesUseCase()
            .subscribeOn(io())
            .observeOn(mainThread())
            .doOnSubscribe { isDeleting = true }
            .doFinally { isDeleting = false }
            .subscribe({
                showAllExpensesDeletedMessage.next()
            }, { e ->
                Log.d(TAG, "Failed to delete all expenses. Cause: `${e.localizedMessage}`.")
            })
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val preferenceDataSource = PreferenceDataSource()

            val databaseDataSource = DatabaseDataSource(application.database)

            val exportExpensesUseCase = ExportExpensesUseCase(databaseDataSource)
            val deleteAllExpensesUseCase = DeleteAllExpensesUseCase(databaseDataSource)

            return SettingsFragmentModel(
                application,
                preferenceDataSource,
                exportExpensesUseCase,
                deleteAllExpensesUseCase
            ) as T
        }
    }

    companion object {

        private const val TAG = "SettingsFragmentModel"

        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1

        private val GITHUB_URI = Uri.parse("https://github.com/Nominalista/Expenses")
    }
}
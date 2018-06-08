package com.nominalista.expenses.`interface`.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.infrastructure.utils.DataEvent
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.task.ExportExpensesTask
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1
private val GITHUB_URI = Uri.parse("https://github.com/Nominalista/Expenses")

class SettingsFragmentModel(
        application: Application,
        private val databaseDataSource: DatabaseDataSource,
        private val preferenceDataSource: PreferenceDataSource
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())
    val showCurrencySelectionDialog = Event()
    val showDeleteAllExpensesDialog = Event()
    val showAllExpensesDeletedMessage = Event()
    val showExpenseExportMessage = DataEvent<String>()
    val showWebsite = DataEvent<Uri>()
    val requestWriteExternalStoragePermission = DataEvent<Int>()

    private var itemModelsDisposable: Disposable? = null

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        itemModelsDisposable = getItemModels().subscribe { itemModels.value = it }
    }

    private fun getItemModels(): Observable<List<SettingItemModel>> {
        return Observable.just(createExpenseSection() + createGeneralSection())
    }

    // Expense section

    private fun createExpenseSection(): List<SettingItemModel> {
        val context = getApplication<Application>()
        var itemModels = listOf<SettingItemModel>()
        itemModels += createExpenseHeader(context)
        itemModels += createDefaultCurrency(context)
        itemModels += createExportExpenses(context)
        itemModels += createDeleteAllExpenses(context)
        return itemModels
    }

    private fun createExpenseHeader(context: Context): SettingItemModel {
        return SettingsHeaderModel(context.getString(R.string.expenses))
    }

    private fun createDefaultCurrency(context: Context): SettingItemModel {
        val currency = preferenceDataSource.getDefaultCurrency(context)
        val title = context.getString(R.string.default_currency)
        val summary = context.getString(R.string.default_currency_summary,
                currency.flag,
                currency.title,
                currency.code)
        val itemModel = SummaryActionSettingItemModel(title, summary)
        itemModel.click = { showCurrencySelectionDialog.next() }
        return itemModel
    }

    private fun createExportExpenses(context: Context): SettingItemModel {
        val title = context.getString(R.string.export_to_excel)
        val itemModel = ActionSettingItemModel(title)
        itemModel.click = {
            requestWriteExternalStoragePermission.next(REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        }
        return itemModel
    }

    private fun createDeleteAllExpenses(context: Context): SettingItemModel {
        val title = context.getString(R.string.delete_all)
        val itemModel = ActionSettingItemModel(title)
        itemModel.click = { showDeleteAllExpensesDialog.next() }
        return itemModel
    }

    // General section

    private fun createGeneralSection(): List<SettingItemModel> {
        val context = getApplication<Application>()
        var itemModels = listOf<SettingItemModel>()
        itemModels += createGeneralHeader(context)
        itemModels += createViewSourceCode(context)
        return itemModels
    }

    private fun createGeneralHeader(context: Context): SettingItemModel {
        return SettingsHeaderModel(context.getString(R.string.general))
    }

    private fun createViewSourceCode(context: Context): SettingItemModel {
        val title = context.getString(R.string.view_source_code)
        val itemModel = ActionSettingItemModel(title)
        itemModel.click = { showWebsite.next(GITHUB_URI) }
        return itemModel
    }

    override fun onCleared() {
        super.onCleared()
        itemModelsDisposable?.dispose()
    }

    // Public

    fun updateDefaultCurrency(currency: Currency) {
        val context = getApplication<Application>()
        preferenceDataSource.setDefaultCurrency(context, currency)
        reloadItemModels()
    }

    fun permissionGranted(requestCode: Int) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) exportExpenses()
    }

    private fun exportExpenses() {
        val task = ExportExpensesTask(getApplication<Application>(), databaseDataSource)
        task.callback = { showExpenseExportMessage(it) }
        task.execute()
    }

    private fun showExpenseExportMessage(isSuccessful: Boolean) {
        val messageResId = if (isSuccessful) R.string.expense_export_success_message else R.string.expense_export_failure_message
        val message = getApplication<Application>().getString(messageResId)
        showExpenseExportMessage.next(message)
    }

    fun deleteAllExpenses() {
        databaseDataSource.deleteAllExpenses()
        showAllExpensesDeletedMessage.next()
    }

    private fun reloadItemModels() {
        itemModelsDisposable?.dispose()
        loadItemModels()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            val preferenceDataSource = PreferenceDataSource()
            return SettingsFragmentModel(application, databaseDataSource, preferenceDataSource) as T
        }
    }
}
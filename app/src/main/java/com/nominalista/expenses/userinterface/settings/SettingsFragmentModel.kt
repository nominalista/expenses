package com.nominalista.expenses.userinterface.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.automaton.ApplicationAutomaton
import com.nominalista.expenses.automaton.settings.SettingsInput.*
import com.nominalista.expenses.automaton.settings.SettingsState.ExpenseExportState
import com.nominalista.expenses.automaton.settings.SettingsState
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.infrastructure.utils.DataEvent
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1
private val GITHUB_URI = Uri.parse("https://github.com/Nominalista/Expenses")

class SettingsFragmentModel(
        application: Application,
        private val automaton: ApplicationAutomaton
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())

    val showCurrencySelectionDialog = Event()
    val showDeleteAllExpensesDialog = Event()
    val showAllExpensesDeletedMessage = Event()
    val showExpenseExportMessage = DataEvent<String>()
    val showWebsite = DataEvent<Uri>()
    val requestWriteExternalStoragePermission = DataEvent<Int>()

    private var automatonDisposable: Disposable? = null
    private var updateDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeAutomaton()
        sendLoadDefaultCurrency(getApplication())
    }

    private fun subscribeAutomaton() {
        automatonDisposable = automaton.state
                .map { it.settingsState }
                .distinctUntilChanged()
                .subscribe { stateChanged(it) }
    }

    private fun stateChanged(state: SettingsState) {
        updateItemModels(state.defaultCurrency)

        if (state.expenseExportState is ExpenseExportState.Finished) {
            showExpenseExportMessage(state.expenseExportState.isSuccessful)
        }
    }

    private fun updateItemModels(defaultCurrency: Currency?) {
        updateDisposable?.dispose()

        if (defaultCurrency == null) {
            itemModels.value = emptyList()
            return
        }

        updateDisposable = Observable.just(defaultCurrency)
                .observeOn(Schedulers.computation())
                .map { createExpenseSection(it) + createGeneralSection() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    itemModels.value = it
                    updateDisposable = null
                }
    }

    // Expense section

    private fun createExpenseSection(defaultCurrency: Currency): List<SettingItemModel> {
        val context = getApplication<Application>()
        var itemModels = listOf<SettingItemModel>()
        itemModels += createExpenseHeader(context)
        itemModels += createDefaultCurrency(context, defaultCurrency)
        itemModels += createExportExpenses(context)
        itemModels += createDeleteAllExpenses(context)
        return itemModels
    }

    private fun createExpenseHeader(context: Context): SettingItemModel {
        return SettingsHeaderModel(context.getString(R.string.expenses))
    }

    private fun createDefaultCurrency(
            context: Context,
            defaultCurrency: Currency
    ): SettingItemModel {
        val title = context.getString(R.string.default_currency)
        val summary = context.getString(R.string.default_currency_summary,
                defaultCurrency.flag,
                defaultCurrency.title,
                defaultCurrency.code)
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

    // Expense export

    private fun showExpenseExportMessage(isSuccessful: Boolean) {
        val messageResId = if (isSuccessful) {
            R.string.expense_export_success_message
        } else {
            R.string.expense_export_failure_message
        }
        val message = getApplication<Application>().getString(messageResId)
        showExpenseExportMessage.next(message)
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        unsubscribeAutomaton()
        sendRestoreState()
    }

    private fun unsubscribeAutomaton() {
        automatonDisposable?.dispose()
        automatonDisposable = null
    }

    // Public

    fun updateDefaultCurrency(defaultCurrency: Currency) {
        val context = getApplication<Application>()
        sendSaveDefaultCurrency(context, defaultCurrency)
    }

    fun permissionGranted(requestCode: Int) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) exportExpenses()
    }

    private fun exportExpenses() {
        if (isExporting()) return
        val context = getApplication<Application>()
        sendExportExpenses(context)
    }

    private fun isExporting()
            = automaton.state.value.settingsState.expenseExportState == ExpenseExportState.Working

    fun deleteAllExpenses() {
        sendDeleteAllExpenses()
        showAllExpensesDeletedMessage.next()
    }

    // Sending inputs

    private fun sendLoadDefaultCurrency(context: Context)
            = automaton.send(LoadDefaultCurrencyInput(context))

    private fun sendSaveDefaultCurrency(context: Context, defaultCurrency: Currency)
            = automaton.send(SaveDefaultCurrencyInput(context, defaultCurrency))

    private fun sendExportExpenses(context: Context) = automaton.send(ExportExpensesInput(context))

    private fun sendDeleteAllExpenses() = automaton.send(DeleteAllExpensesInput)

    private fun sendRestoreState() = automaton.send(RestoreStateInput)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsFragmentModel(application, application.automaton) as T
        }
    }
}
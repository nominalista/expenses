package com.nominalista.expenses.settings.presentation

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.nominalista.expenses.Application
import com.nominalista.expenses.BuildConfig
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.settings.work.ExpenseDeletionWorker
import com.nominalista.expenses.settings.work.ExpenseExportWorker
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class SettingsFragmentModel(
    application: Application,
    private val preferenceDataSource: PreferenceDataSource
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())

    val showCurrencySelectionDialog = Event()
    val showDeleteAllExpensesDialog = Event()

    val showExpenseExportMessage = DataEvent<Int>()
    val showExpensesDeletionMessage = DataEvent<Int>()

    val showActivity = DataEvent<Uri>()
    val shareData = DataEvent<String>()

    val requestWriteExternalStoragePermission = DataEvent<Int>()

    val observeWorkInfo = DataEvent<UUID>()

    private var expenseExportId: UUID? = null
    private var expenseDeletionId: UUID? = null

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        itemModels.value =
            createExpenseSection() + createGeneralSection() + createInformationSection()
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
        itemModels += createShareApp(context)
        itemModels += createRateApp(context)
        itemModels += createContactMe(context)

        return itemModels
    }

    private fun createGeneralHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.general))

    private fun createViewSourceCode(context: Context): SettingItemModel {
        val title = context.getString(R.string.view_source_code)

        return ActionSettingItemModel(title).apply {
            click = { showActivity.next(GITHUB_URI) }
        }
    }

    private fun createShareApp(context: Context): SettingItemModel {
        val title = context.getString(R.string.share_app)

        return ActionSettingItemModel(title).apply {
            click = { shareData.next(GOOGLE_PLAY_URI.toString()) }
        }
    }

    private fun createRateApp(context: Context): SettingItemModel {
        val title = context.getString(R.string.rate_app)

        return ActionSettingItemModel(title).apply {
            click = { showActivity.next(GOOGLE_PLAY_URI) }
        }
    }

    private fun createContactMe(context: Context): SettingItemModel {
        val title = context.getString(R.string.contact_me)
        val summary = context.getString(R.string.feedback_is_welcome)

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showActivity.next(EMAIL_URI) }
        }
    }

    // Information section

    private fun createInformationSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()

        itemModels += createInformationHeader(context)
        itemModels += createPrivacyPolicy(context)
        itemModels += createVersion(context)

        return itemModels
    }

    private fun createInformationHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.information))

    private fun createPrivacyPolicy(context: Context): SettingItemModel {
        val title = context.getString(R.string.privacy_policy)

        return ActionSettingItemModel(title).apply {
            click = { showActivity.next(PRIVACY_POLICY_URI) }
        }
    }

    private fun createVersion(context: Context): SettingItemModel {
        val title = context.getString(R.string.version)
        val summary = BuildConfig.VERSION_NAME

        return SummaryActionSettingItemModel(title, summary)
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
        if (expenseExportId != null) return

        val id = ExpenseExportWorker.enqueue()
        expenseExportId = id
        observeWorkInfo.next(id)
    }

    fun deleteAllExpenses() {
        if (expenseDeletionId != null) return

        val id = ExpenseDeletionWorker.enqueue()
        expenseDeletionId = id
        observeWorkInfo.next(id)
    }

    fun handleWorkInfo(workInfo: WorkInfo) {
        when (workInfo.id) {
            expenseExportId -> handleExpenseExportWorkInfo(workInfo)
            expenseDeletionId -> handleExpenseDeletionWorkInfo(workInfo)
        }
    }

    private fun handleExpenseExportWorkInfo(workInfo: WorkInfo) {
        expenseExportId = when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                showExpenseExportMessage.next(R.string.expense_export_success_message)
                null
            }
            WorkInfo.State.FAILED -> {
                showExpenseExportMessage.next(R.string.expense_export_failure_message)
                null
            }
            else -> return
        }
    }

    private fun handleExpenseDeletionWorkInfo(workInfo: WorkInfo) {
        expenseDeletionId = when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                showExpensesDeletionMessage.next(R.string.expense_deletion_success_message)
                null
            }
            WorkInfo.State.FAILED -> {
                showExpensesDeletionMessage.next(R.string.expense_deletion_failure_message)
                null
            }
            else -> return
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsFragmentModel(application, PreferenceDataSource()) as T
        }
    }

    companion object {

        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1

        private val GITHUB_URI =
            Uri.parse("https://github.com/Nominalista/Expenses")

        private val GOOGLE_PLAY_URI =
            Uri.parse("https://play.google.com/store/apps/details?id=com.nominalista.expenses")

        private val EMAIL_URI =
            Uri.parse("mailto:the.nominalista@gmail.com")

        private val PRIVACY_POLICY_URI =
            Uri.parse("https://github.com/nominalista/expenses/blob/master/privacy_policy.md")
    }
}
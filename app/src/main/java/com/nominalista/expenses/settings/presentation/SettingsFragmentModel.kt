package com.nominalista.expenses.settings.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.R
import com.nominalista.expenses.authentication.AuthenticationManager
import com.nominalista.expenses.common.presentation.Theme
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SettingsFragmentModel(
    application: Application,
    private val preferenceDataSource: PreferenceDataSource,
    private val authenticationManager: AuthenticationManager,
    private val dataStore: DataStore
) : AndroidViewModel(application) {
    val itemModels = Variable(emptyList<SettingItemModel>())
    val selectDefaultCurrency = Event()
    val navigateToOnboarding = Event()
    val showMessage = DataEvent<Int>()
    val showActivity = DataEvent<Uri>()
    val showThemeSelectionDialog = DataEvent<Theme>()
    val applyTheme = DataEvent<Theme>()
    val requestSmsPermission = Event()
    val manageRules = Event()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        loadItemModels()
    }

    fun loadItemModels() {
        itemModels.value =
            createAccountSection() + createApplicationSection() + createPrivacySection()
    }

    // Account section

    private fun createAccountSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()
        itemModels += createAccountHeader(context)
        itemModels += if (authenticationManager.isUserSignedIn()) {
            createSignOut(context)
        } else {
            signUpOrSignIn(context)
        }

        return itemModels
    }

    private fun createAccountHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.account))

    private fun createSignOut(context: Context): SettingItemModel {
        val title = context.getString(R.string.sign_out)

        return ActionSettingItemModel(title).apply {
            click = {
                authenticationManager.signOut()
                preferenceDataSource.setIsUserOnboarded(getApplication(), false)
                navigateToOnboarding.next()
            }
        }
    }

    private fun signUpOrSignIn(context: Context): SettingItemModel {
        val title = context.getString(R.string.sign_up_or_sign_in)

        return ActionSettingItemModel(title).apply {
            click = {
                preferenceDataSource.setIsUserOnboarded(getApplication(), false)
                navigateToOnboarding.next()
            }
        }
    }

    // Application section

    private fun createApplicationSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()
        itemModels += createApplicationHeader(context)
        itemModels += createDefaultCurrency(context)
        itemModels += createDarkMode(context)
        itemModels += createSmsReader(context)

        if (preferenceDataSource.getIsSmsReaderEnabled(context)) {
            itemModels += createRules(context)
        }

        return itemModels
    }

    private fun createApplicationHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.application))

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
            click = { selectDefaultCurrency.next() }
        }
    }

    private fun createDarkMode(context: Context): SettingItemModel {
        val title = context.getString(R.string.theme)

        val darkMode = preferenceDataSource.getTheme(context)

        val summary = when (darkMode) {
            Theme.LIGHT -> context.getString(R.string.light)
            Theme.DARK -> context.getString(R.string.dark)
            Theme.SYSTEM_DEFAULT -> context.getString(R.string.system_default)
        }

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showThemeSelectionDialog.next(darkMode) }
        }
    }

    private fun createSmsReader(context: Context): SettingItemModel {
        val title = context.getString(R.string.sms_reader)

        val enabled = preferenceDataSource.getIsSmsReaderEnabled(context)

        return SwitchSettingItemModel(title, enabled).apply {
            isChecked = { isChecked ->
                getApplication<Application>().let {
                    preferenceDataSource.setIsSmsReaderEnabled(it, isChecked)
                }
                loadItemModels()
                if (isChecked){
                    requestSmsPermission.next()
                }
            }
        }
    }

    private fun createRules(context: Context): SettingItemModel {

        val title = context.getString(R.string.sms_reader_rules)

        val rules = dataStore.getRules()
                .subscribeOn(Schedulers.io())
                .blockingGet()

        val summary = when {
            rules.isEmpty() -> context.getString(R.string.sms_reader_rules_placeholder)
            else -> rules.map { it.keywords.first() }.reduce { acc, next -> "$acc, $next" }
        }
        return SummaryActionSettingItemModel(title, summary).apply {
            click = { manageRules.next() }
        }
    }

    // About section

    private fun createPrivacySection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()
        itemModels += createPrivacyHeader(context)
        itemModels += createPrivacyPolicy(context)

        return itemModels
    }

    private fun createPrivacyHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.privacy))

    private fun createPrivacyPolicy(context: Context): SettingItemModel {
        val title = context.getString(R.string.privacy_policy)

        return ActionSettingItemModel(title).apply {
            click = { showActivity.next(PRIVACY_POLICY_URI) }
        }
    }
    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    // Public

    fun defaultCurrencySelected(defaultCurrency: Currency) {
        getApplication<Application>().let {
            preferenceDataSource.setDefaultCurrency(it, defaultCurrency)
        }

        loadItemModels()
    }

    fun themeSelected(theme: Theme) {
        getApplication<Application>().let {
            preferenceDataSource.setTheme(it, theme)
        }

        loadItemModels()

        applyTheme.next(theme)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsFragmentModel(
                    application,
                    application.preferenceDataSource,
                    application.authenticationManager,
                    application.localDataStore
            ) as T
        }
    }

    companion object {
        private val PRIVACY_POLICY_URI =
            Uri.parse("https://raw.githubusercontent.com/nominalista/expenses/master/resources/privacy_policy.md")
    }
}
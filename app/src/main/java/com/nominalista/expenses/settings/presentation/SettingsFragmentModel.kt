package com.nominalista.expenses.settings.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.nominalista.expenses.Application
import com.nominalista.expenses.BuildConfig
import com.nominalista.expenses.R
import com.nominalista.expenses.authentication.AuthenticationManager
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Theme
import com.nominalista.expenses.data.preference.PreferenceDataSource
import com.nominalista.expenses.settings.work.ExpenseExportWorker
import com.nominalista.expenses.settings.work.ExpenseImportWorker
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.disposables.CompositeDisposable

class SettingsFragmentModel(
    application: Application,
    private val preferenceDataSource: PreferenceDataSource,
    private val authenticationManager: AuthenticationManager
) : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())

    val selectDefaultCurrency = Event()
    val navigateToOnboarding = Event()

    val showMessage = DataEvent<Int>()
    val showActivity = DataEvent<Uri>()
    val showThemeSelectionDialog = DataEvent<Theme>()
    val applyTheme = DataEvent<Theme>()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        itemModels.value =
            createAccountSection() + createApplicationSection() + createAboutSection()
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
        itemModels += createTheme(context)

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

    private fun createTheme(context: Context): SettingItemModel {
        val title = context.getString(R.string.theme)

        val currentTheme = preferenceDataSource.getTheme(context)

        val summary = when (currentTheme) {
            Theme.LIGHT -> context.getString(R.string.light)
            Theme.DARK -> context.getString(R.string.dark)
            Theme.AUTO -> context.getString(R.string.auto)
        }

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showThemeSelectionDialog.next(currentTheme) }
        }
    }

    // About section

    private fun createAboutSection(): List<SettingItemModel> {
        val context = getApplication<Application>()

        val itemModels = mutableListOf<SettingItemModel>()
        itemModels += createAboutHeader(context)
        itemModels += createContactMe(context)
        itemModels += createRateApp(context)
        itemModels += createViewSourceCode(context)
        itemModels += createPrivacyPolicy(context)
        itemModels += createVersion(context)

        return itemModels
    }

    private fun createAboutHeader(context: Context): SettingItemModel =
        SettingsHeaderModel(context.getString(R.string.about))

    private fun createContactMe(context: Context): SettingItemModel {
        val title = context.getString(R.string.contact_me)
        val summary = context.getString(R.string.contact_me_summary)

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showActivity.next(EMAIL_URI) }
        }
    }

    private fun createRateApp(context: Context): SettingItemModel {
        val title = context.getString(R.string.rate_app)
        val summary = context.getString(R.string.rate_app_summary)

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showActivity.next(GOOGLE_PLAY_URI) }
        }
    }

    private fun createViewSourceCode(context: Context): SettingItemModel {
        val title = context.getString(R.string.view_source_code)
        val summary = context.getString(R.string.view_source_code_summary)

        return SummaryActionSettingItemModel(title, summary).apply {
            click = { showActivity.next(GITHUB_URI) }
        }
    }

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
            val preferenceDataSource = PreferenceDataSource()
            val authenticationManager = AuthenticationManager.getInstance(application)
            return SettingsFragmentModel(
                application,
                preferenceDataSource,
                authenticationManager
            ) as T
        }
    }

    companion object {

        private val EMAIL_URI =
            Uri.parse("mailto:the.nominalista@gmail.com")
        private val GOOGLE_PLAY_URI =
            Uri.parse("https://play.google.com/store/apps/details?id=com.nominalista.expenses")
        private val GITHUB_URI =
            Uri.parse("https://github.com/Nominalista/Expenses")
        private val PRIVACY_POLICY_URI =
            Uri.parse("https://raw.githubusercontent.com/nominalista/expenses/master/resources/privacy_policy.md")
    }
}
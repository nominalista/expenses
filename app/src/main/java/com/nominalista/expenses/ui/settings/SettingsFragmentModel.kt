package com.nominalista.expenses.ui.settings

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import com.nominalista.expenses.infrastructure.utils.runOnBackground
import com.nominalista.expenses.model.ApplicationDatabase
import com.nominalista.expenses.model.Currency
import com.nominalista.expenses.model.User
import com.nominalista.expenses.source.PreferenceDataSource
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

class SettingsFragmentModel(
        application: Application,
        private val database: ApplicationDatabase,
        private val preferenceDataSource: PreferenceDataSource)
    : AndroidViewModel(application) {

    val itemModels = Variable(emptyList<SettingItemModel>())
    val showAddUserDialog = Event()
    val showCurrencySelectionDialog = Event()

    private var itemModelsDisposable: Disposable? = null

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        itemModelsDisposable = getItemModels()
                .subscribe { itemModels -> this.itemModels.value = itemModels }
    }

    private fun getItemModels(): Observable<List<SettingItemModel>> {
        return Observable.combineLatest(getUserSection(),
                getOtherSection(),
                BiFunction { userSection, otherSection -> userSection + otherSection })
    }

    // User section

    private fun getUserSection(): Observable<List<SettingItemModel>> {
        return getUserItemModels()
                .map { userItemModels ->
                    var itemModels = listOf<SettingItemModel>(createUserHeaderModel())
                    itemModels += userItemModels
                    itemModels += createAddUserViewModel()
                    itemModels
                }
    }

    private fun getUserItemModels(): Observable<List<SettingItemModel>> {
        return database.userDao()
                .getAll()
                .toObservable()
                .map { users -> users.map { user -> createUserItemModel(user) } }
    }

    private fun createUserItemModel(user: User): UserItemModel {
        val viewModel = UserItemModel(user)
        viewModel.deleteButtonClick = { deleteUser(user) }
        return viewModel
    }

    private fun deleteUser(user: User) {
        runOnBackground { database.userDao().delete(user) }
    }

    private fun createUserHeaderModel(): UserHeaderModel {
        return UserHeaderModel()
    }

    private fun createAddUserViewModel(): AddUserItemModel {
        val viewModel = AddUserItemModel()
        viewModel.click = { showAddUserDialog.next() }
        return viewModel
    }

    // Other section

    private fun getOtherSection(): Observable<List<SettingItemModel>> {
        var itemModels = listOf<SettingItemModel>(createOtherHeaderModel())
        itemModels += createDefaultCurrencyItemModel()
        return Observable.just(itemModels)
    }

    private fun createOtherHeaderModel(): OtherHeaderModel {
        return OtherHeaderModel()
    }

    private fun createDefaultCurrencyItemModel(): DefaultCurrencyItemModel {
        val context = getApplication<Application>()
        val defaultCurrency = preferenceDataSource.getDefaultCurrency(context)
        val itemModel = DefaultCurrencyItemModel(defaultCurrency)
        itemModel.click = { showCurrencySelectionDialog.next() }
        return itemModel
    }

    fun updateDefaultCurrency(currency: Currency) {
        val context = getApplication<Application>()
        preferenceDataSource.setDefaultCurrency(context, currency)
        reloadItemModels()
    }

    private fun reloadItemModels() {
        itemModelsDisposable?.dispose()
        loadItemModels()
    }

    override fun onCleared() {
        super.onCleared()
        itemModelsDisposable?.dispose()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsFragmentModel(application, application.database, PreferenceDataSource()) as T
        }
    }
}
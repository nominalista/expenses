package com.nominalista.expenses.sms.rule

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.sms.MessageParser
import com.nominalista.expenses.util.extensions.TAG
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.SchedulerTransformer
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RuleFragmentModel(private val dataStore: DataStore, private val db: FirebaseFirestore) : ViewModel() {

    val itemModels = Variable(emptyList<RuleItemModel>())
    val showNewRule = Event()
    val finish = Event()

    val showEditRule = DataEvent<Rule>()
    val showUserRuleDialog = DataEvent<Rule>()

    private val disposables = CompositeDisposable()
    private var ruleList = emptyList<Rule>()

    // Lifecycle start

    init {
        observeRules()
    }

    private fun observeRules() {
        disposables += dataStore.observeRules()
                .compose(SchedulerTransformer())
                .subscribe({ rules ->
                    Log.d(TAG, "Rule observation updated.")
                    ruleList = rules
                    updateItemModels(rules)
                }, { error ->
                    Log.d(TAG, "Rule observation failed (${error.message}).")
                })
    }

    private fun createAddRuleSection() = listOf(createAddRuleItemModel())

    private fun createAddRuleItemModel(): AddRuleItemModel {
        return AddRuleItemModel().apply { click = { showNewRule.next() } }
    }

    private fun createRuleSection(rules: List<Rule>) = rules.map { createRuleItemModel(it) }

    private fun createRuleItemModel(rule: Rule): RuleItemModel {
        val itemModel = RuleItemModelImpl(rule)
        itemModel.editClick = { editRule(itemModel) }
        itemModel.deleteClick = { deleteRule(itemModel) }
        itemModel.duplicateClick = { createRule(itemModel.rule) }
        itemModel.shareClick = { shareRule(itemModel.rule) }
        itemModel.useClick = { showUserRuleDialog.next(itemModel.rule) }
        return itemModel
    }

    private fun editRule(itemModel: RuleItemModelImpl) {
        showEditRule.next(itemModel.rule)
    }

    private fun deleteRule(itemModel: RuleItemModelImpl) {
        val rule = itemModel.rule

        disposables += dataStore.deleteRule(rule)
                .compose(SchedulerTransformer<Any>())
                .subscribe({
                    Log.d(TAG, "Rule #${rule.id} deletion succeeded.")
                }, { error ->
                    Log.d(TAG, "Rule #${rule.id} deletion failed (${error.message}..")
                })
    }

    private fun shareRule(rule: Rule) {
        db.collection("Rules").add(rule)
    }

    fun useRule(rule: Rule, message: String, applicationContext: Context) {
        val defaultCurrency = applicationContext.application.preferenceDataSource.getDefaultCurrency(applicationContext.application)
        val localDataStore = applicationContext.application.localDataStore
        disposables += MessageParser.insertExpense(localDataStore, AndroidSchedulers.mainThread(), Schedulers.io(), defaultCurrency, rule, message)
        finish.next()
    }

    private fun getGlobalRules() {
        db.collection("Rules")
                .get()
                .addOnSuccessListener { documents ->
                    val rules: List<RemoteRuleItemModelImpl> = documents.map { RemoteRuleItemModelImpl(Rule(it)) }
                    val unUsed = rules.filter { remote ->
                        !ruleList
                                .map { it.keywords == remote.name }
                                .fold(false) { acc, next -> acc || next }
                    }
                            .map {
                                it.useClick = {
                                    createRule(it.rule)
                                }
                                it
                            }
                    itemModels.value = itemModels.value + unUsed
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
    }

    private fun updateItemModels(tags: List<Rule>) {
        itemModels.value = tags
                .sortedBy { it.keywords.first() }
                .let { createAddRuleSection() + createRuleSection(it) }
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    // Public

    fun createRule(rule: Rule) {
        disposables += dataStore.insertRule(rule)
                .compose(SchedulerTransformer<Any>())
                .subscribe({
                    Log.d(TAG, "Rule insertion succeeded.")
                }, { error ->
                    Log.d(TAG, "Rule insertion failed (${error.localizedMessage}).")
                })
    }

    fun updateRule(rule: Rule) {
        disposables += dataStore.updateRule(rule)
                .compose(SchedulerTransformer<Any>())
                .subscribe({
                    Log.d(TAG, "Rule update succeeded.")
                }, { error ->
                    Log.d(TAG, "Rule update failed (${error.localizedMessage}).")
                })
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RuleFragmentModel(application.localDataStore, FirebaseFirestore.getInstance()) as T
        }
    }
}
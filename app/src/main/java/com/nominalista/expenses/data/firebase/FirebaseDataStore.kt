package com.nominalista.expenses.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.extensions.toEpochMillis
import com.nominalista.expenses.util.extensions.toLocalDate
import com.nominalista.expenses.util.reactive.ReactiveDocumentSnapshotEventListener
import com.nominalista.expenses.util.reactive.ReactiveQuerySnapshotEventListener
import com.nominalista.expenses.util.reactive.ReactiveTaskListener
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FirebaseDataStore(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : DataStore {

    // Expenses

    override fun observeExpenses(): Observable<List<Expense>> {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Observable.error(ReferenceAccessError())

        val listener = ReactiveQuerySnapshotEventListener(expenseCollectionReference)

        return Observable.create(listener)
            .map { query -> query.mapNotNull { mapDocumentToExpense(it) } }
    }

    override fun getExpenses(): Single<List<Expense>> {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Single.error(ReferenceAccessError())

        val listener = ReactiveTaskListener(expenseCollectionReference.get())

        return Single.create(listener)
            .map { query -> query.mapNotNull { mapDocumentToExpense(it) } }
    }

    override fun observeExpense(id: String): Observable<Expense> {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Observable.error(ReferenceAccessError())

        val expenseDocumentReference = expenseCollectionReference.document(id)

        val listener = ReactiveDocumentSnapshotEventListener(expenseDocumentReference)

        return Observable.create(listener)
            .map { document -> mapDocumentToExpense(document) }
    }

    override fun getExpense(id: String): Single<Expense> {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Single.error(ReferenceAccessError())

        val expenseDocumentReference = expenseCollectionReference.document(id)

        val listener = ReactiveTaskListener(expenseDocumentReference.get())

        return Single.create(listener)
            .map { document -> mapDocumentToExpense(document) }
    }

    override fun insertExpense(expense: Expense): Single<String> {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Single.error(ReferenceAccessError())

        val data = hashMapOf(
            "amount" to expense.amount,
            "currency" to expense.currency.code,
            "title" to expense.title,
            "tags" to expense.tags.map { mapOf("id" to it.id, "name" to it.name) },
            "date" to expense.date.toEpochMillis(),
            "notes" to expense.notes,
            "timestamp" to FieldValue.serverTimestamp()
        )

        return Single.fromCallable {
            val document = expenseCollectionReference.document()
            document.set(data)
            document.id
        }
    }

    override fun updateExpense(expense: Expense): Completable {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Completable.error(ReferenceAccessError())

        val expenseDocumentReference = expenseCollectionReference.document(expense.id)

        val data = hashMapOf(
            "amount" to expense.amount,
            "currency" to expense.currency.code,
            "title" to expense.title,
            "tags" to expense.tags.map { mapOf("id" to it.id, "name" to it.name) },
            "date" to expense.date.toEpochMillis(),
            "notes" to expense.notes
        )

        return Completable.fromAction { expenseDocumentReference.update(data) }
    }

    override fun deleteExpense(expense: Expense): Completable {
        val expenseCollectionReference = getExpenseCollectionReference()
            ?: return Completable.error(ReferenceAccessError())

        val expenseDocumentReference = expenseCollectionReference.document(expense.id)

        return Completable.fromAction { expenseDocumentReference.delete() }
    }

    /**
     * Firebase does not support deleting collection. Get all expenses and delete one by one.
     */
    override fun deleteAllExpenses(): Completable {
        return getExpenses()
            .flatMapCompletable { expenses ->
                Completable.merge(expenses.map { deleteExpense(it) })
            }
    }

    // Tags

    override fun observeTags(): Observable<List<Tag>> {
        val tagCollectionReference = getTagCollectionReference()
            ?: return Observable.error(ReferenceAccessError())

        val listener = ReactiveQuerySnapshotEventListener(tagCollectionReference)

        return Observable.create(listener)
            .map { query -> query.mapNotNull { mapDocumentToTag(it) } }
    }

    override fun getTags(): Single<List<Tag>> {
        val tagCollectionReference = getTagCollectionReference()
            ?: return Single.error(ReferenceAccessError())

        val listener = ReactiveTaskListener(tagCollectionReference.get())

        return Single.create(listener)
            .map { query -> query.mapNotNull { mapDocumentToTag(it) } }
    }

    override fun insertTag(tag: Tag): Single<String> {
        val tagCollectionReference = getTagCollectionReference()
            ?: return Single.error(ReferenceAccessError())

        val data = hashMapOf("name" to tag.name)

        return Single.fromCallable {
            val document = tagCollectionReference.document()
            document.set(data)
            document.id
        }
    }

    override fun deleteTag(tag: Tag): Completable {
        val tagCollectionReference = getTagCollectionReference()
            ?: return Completable.error(ReferenceAccessError())

        val tagDocumentReference = tagCollectionReference.document(tag.id)

        return Completable.fromAction { tagDocumentReference.delete() }
    }

    override fun deleteAllTags(): Completable {
        throw NotImplementedError("Deleting a collection in Firestore is impossible.")
    }

    // Rules

    override fun observeRules(): Observable<List<Rule>> {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Observable.error(ReferenceAccessError())

        val listener = ReactiveQuerySnapshotEventListener(ruleCollectionReference)

        return Observable.create(listener)
                .map { query -> query.mapNotNull { mapDocumentToRule(it) } }
    }

    override fun getRules(): Single<List<Rule>> {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Single.error(ReferenceAccessError())

        val listener = ReactiveTaskListener(ruleCollectionReference.get())

        return Single.create(listener)
                .map { query -> query.mapNotNull { mapDocumentToRule(it) } }
    }

    override fun observeRule(id: String): Observable<Rule> {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Observable.error(ReferenceAccessError())

        val ruleDocumentReference = ruleCollectionReference.document(id)

        val listener = ReactiveDocumentSnapshotEventListener(ruleDocumentReference)

        return Observable.create(listener)
                .map { document -> mapDocumentToRule(document) }
    }

    override fun getRule(id: String): Single<Rule> {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Single.error(ReferenceAccessError())

        val ruleDocumentReference = ruleCollectionReference.document(id)

        val listener = ReactiveTaskListener(ruleDocumentReference.get())

        return Single.create(listener)
                .map { document -> mapDocumentToRule(document) }
    }

    override fun insertRule(rule: Rule): Single<String> {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Single.error(ReferenceAccessError())

        val data = hashMapOf("name" to rule.keywords)

        return Single.fromCallable {
            val document = ruleCollectionReference.document()
            document.set(data)
            document.id
        }
    }

    override fun updateRule(rule: Rule): Completable {
        val ruleCollectionReference = getRuleCollectionReference() ?: return Completable.error(ReferenceAccessError())

        val ruleDocumentReference = ruleCollectionReference.document(rule.id)

        val data = hashMapOf("name" to rule.keywords,
                "firstSymbol" to rule.firstSymbol,
                "decimalSeparator" to rule.decimalSeparator,
                "groupSeparator" to rule.groupSeparator)
        return Completable.fromAction { ruleDocumentReference.update(data as Map<String, Any>) }
    }

    override fun deleteRule(rule: Rule): Completable {
        val ruleCollectionReference = getRuleCollectionReference()
                ?: return Completable.error(ReferenceAccessError())

        val tagDocumentReference = ruleCollectionReference.document(rule.id)

        return Completable.fromAction { tagDocumentReference.delete() }
    }

    // Helpers

    private fun getExpenseCollectionReference(): CollectionReference? {
        return getUserDataReference()?.collection("expenses")
    }

    private fun getTagCollectionReference(): CollectionReference? {
        return getUserDataReference()?.collection("tags")
    }

    private fun getUserDataReference(): DocumentReference? {
        return auth.currentUser?.uid?.let { firestore.collection("user-data").document(it) }
    }

    private fun getRuleCollectionReference(): CollectionReference? {
        return getUserDataReference()?.collection("rules")
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocumentToExpense(document: DocumentSnapshot): Expense? {
        val amount = document.getDouble("amount")
            ?: return null

        val currency = document.getString("currency")?.let { Currency.fromCode(it) }
            ?: return null

        val title = document.getString("title")
            ?: return null

        val tags = (document.get("tags") as? List<Any>)
            ?.mapNotNull { it as? Map<Any, Any> }
            ?.mapNotNull { mapMapToTag(it) }
            ?: return null

        val date = document.getLong("date")?.toLocalDate()
            ?: return null

        val notes = document.getString("notes")
            ?: return null

        val timestamp = document.getTimestamp("timestamp")?.toDate()?.time

        return Expense(
            document.id,
            amount,
            currency,
            title,
            tags,
            date,
            notes,
            timestamp
        )
    }

    private fun mapMapToTag(map: Map<Any, Any>): Tag? {
        val id = map["id"] as? String ?: return null
        val name = map["name"] as? String ?: return null
        return Tag(id, name)
    }

    private fun mapDocumentToTag(document: DocumentSnapshot): Tag? {
        val name = document.getString("name") ?: return null
        return Tag(document.id, name)
    }

    private fun mapDocumentToRule(document: DocumentSnapshot): Rule? {
        val name = document.getString("name") ?: return null
        val firstSymbol = document.getString("firstSymbol") ?: return null
        val decimalSeparator = document.getString("decimalSeparator") ?: return null
        val groupSeparator = document.getString("groupSeparator") ?: return null
        return Rule(document.id, name.split("\n"), firstSymbol, decimalSeparator, groupSeparator)
    }

    class ReferenceAccessError : Error("Cannot access reference.")
}
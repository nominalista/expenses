package com.nominalista.expenses.addeditexpense.presentation.tagselection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.data.store.DataStore
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.reactive.DataEvent
import com.nominalista.expenses.util.reactive.Event
import com.nominalista.expenses.util.reactive.SchedulerTransformer
import com.nominalista.expenses.util.reactive.Variable
import io.reactivex.disposables.CompositeDisposable

class TagSelectionFragmentModel(private val dataStore: DataStore) : ViewModel() {

    val itemModels = Variable(emptyList<TagSelectionItemModel>())
    val showNewTagDialog = Event()
    val delegateSelectedTags = DataEvent<List<Tag>>()
    val finish = Event()

    private val checkedTags: MutableSet<Tag> = HashSet()

    private val disposables = CompositeDisposable()

    // Lifecycle start

    init {
        observeTags()
    }

    private fun observeTags() {
        disposables += dataStore.observeTags()
            .compose(SchedulerTransformer())
            .subscribe({ tags ->
                Log.d(TAG, "Tag observation updated.")
                updateItemModels(tags)
            }, { error ->
                Log.d(TAG, "Tag observation failed (${error.message}).")
            })
    }

    private fun updateItemModels(tags: List<Tag>) {
        itemModels.value = tags
            .sortedBy { it.name }
            .let { createAddTagSection() + createTagSection(it) }
    }

    private fun createAddTagSection() = listOf(createAddTagItemModel())

    private fun createAddTagItemModel(): AddTagItemModel {
        return AddTagItemModel().apply { click = { showNewTagDialog.next() } }
    }

    private fun createTagSection(tags: List<Tag>) = tags.map { createTagItemModel(it) }

    private fun createTagItemModel(tag: Tag): TagItemModel {
        val itemModel = TagItemModel(tag)
        itemModel.checkClick = { checkTag(itemModel) }
        itemModel.deleteClick = { deleteTag(itemModel) }
        return itemModel
    }

    private fun checkTag(itemModel: TagItemModel) {
        if (itemModel.isChecked) {
            itemModel.isChecked = false
            checkedTags.remove(itemModel.tag)
        } else {
            itemModel.isChecked = true
            checkedTags.add(itemModel.tag)
        }
    }

    private fun deleteTag(itemModel: TagItemModel) {
        val tag = itemModel.tag

        checkedTags.remove(tag)

        disposables += dataStore.deleteTag(tag)
            .compose(SchedulerTransformer<Any>())
            .subscribe({
                Log.d(TAG, "Tag #${tag.id} deletion succeeded.")
            }, { error ->
                Log.d(TAG, "Tag #${tag.id} deletion failed (${error.message}..")
            })
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

    fun createTag(tag: Tag) {
        disposables += dataStore.insertTag(tag)
            .compose(SchedulerTransformer<Any>())
            .subscribe({
                Log.d(TAG, "Tag insertion succeeded.")
            }, { error ->
                Log.d(TAG, "Tag insertion failed (${error.localizedMessage}).")
            })
    }

    fun confirm() {
        delegateSelectedTags.next(checkedTags.toList())
        finish.next()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TagSelectionFragmentModel(application.defaultDataStore) as T
        }
    }

    companion object {
        private val TAG = TagSelectionFragmentModel::class.java.simpleName
    }
}
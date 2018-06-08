package com.nominalista.expenses.userinterface.newexpense.tagselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TagSelectionFragmentModel(private val databaseDataSource: DatabaseDataSource) : ViewModel() {

    val itemModels = Variable(emptyList<TagSelectionItemModel>())
    val selectedTags = ArrayList<Tag>()
    val showNewTagDialog = Event()

    private val compositeDisposable = CompositeDisposable()

    init {
        loadItemModels()
    }

    private fun loadItemModels() {
        compositeDisposable += getTags()
                .observeOn(Schedulers.io())
                .map { sortTags(it) }
                .map { createTagSection(it) + createAddTagSection() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { itemModels.value = it }
    }

    private fun getTags() = databaseDataSource.getTags()

    private fun sortTags(tags: List<Tag>) = tags.sortedBy { it.name }

    private fun createTagSection(tags: List<Tag>) = tags.map { createTagItemModel(it) }

    private fun createTagItemModel(tag: Tag): TagItemModel {
        val itemModel = TagItemModel(tag)
        itemModel.selectClick = { selectTag(itemModel) }
        itemModel.removeClick = { removeTag(itemModel) }
        return itemModel
    }

    private fun selectTag(itemModel: TagItemModel) {
        if (itemModel.isSelected) {
            selectedTags.remove(itemModel.tag)
            itemModel.isSelected = false
        } else {
            selectedTags.add(itemModel.tag)
            itemModel.isSelected = true
        }
    }

    private fun removeTag(itemModel: TagItemModel) {
        selectedTags.remove(itemModel.tag)
        databaseDataSource.deleteTag(itemModel.tag)
    }

    private fun createAddTagSection() = listOf(createAddTagItemModel())

    private fun createAddTagItemModel(): AddTagItemModel {
        val itemModel = AddTagItemModel()
        itemModel.click = { showNewTagDialog.next() }
        return itemModel
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val databaseDataSource = DatabaseDataSource(application.database)
            return TagSelectionFragmentModel(
                    databaseDataSource) as T
        }
    }
}
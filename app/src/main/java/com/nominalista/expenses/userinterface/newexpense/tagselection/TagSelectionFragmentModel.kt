package com.nominalista.expenses.userinterface.newexpense.tagselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.Application
import com.nominalista.expenses.automaton.ApplicationAutomaton
import com.nominalista.expenses.automaton.newexpense.NewExpenseInputs.SetSelectedTagsInput
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionInputs.*
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionState
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.infrastructure.utils.Event
import com.nominalista.expenses.infrastructure.utils.Variable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class TagSelectionFragmentModel(private val automaton: ApplicationAutomaton) : ViewModel() {

    val itemModels = Variable(emptyList<TagSelectionItemModel>())
    val showNewTagDialog = Event()

    private var automatonDisposable: Disposable? = null
    private var updateDisposable: Disposable? = null

    // Lifecycle start

    init {
        subscribeAutomaton()
        sendLoadTags()
    }

    private fun subscribeAutomaton() {
        automatonDisposable = automaton.state
                .map { it.newExpenseState.tagSelectionState }
                .distinctUntilChanged()
                .subscribe { stateChanged(it) }
    }

    private fun stateChanged(state: TagSelectionState) {
        updateItemModels(state.tags)
    }

    private fun updateItemModels(tags: List<Tag>) {
        updateDisposable?.dispose()
        updateDisposable = Observable.just(tags)
                .observeOn(Schedulers.computation())
                .map { sortTags(it) }
                .map { createTagSection(it) + createAddTagSection() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    itemModels.value = it
                    updateDisposable = null
                }
    }

    private fun sortTags(tags: List<Tag>) = tags.sortedBy { it.name }

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
            sendUncheckTag(itemModel.tag)
        } else {
            itemModel.isChecked = true
            sendCheckTag(itemModel.tag)
        }
    }

    private fun deleteTag(itemModel: TagItemModel) {
        val tag = itemModel.tag
        sendUncheckTag(tag)
        sendDeleteTag(tag)
    }

    private fun createAddTagSection() = listOf(createAddTagItemModel())

    private fun createAddTagItemModel(): AddTagItemModel {
        val itemModel = AddTagItemModel()
        itemModel.click = { showNewTagDialog.next() }
        return itemModel
    }

    // Lifecycle end

    override fun onCleared() {
        super.onCleared()
        unsubscribeAutomaton()
        maybeDisposeUpdate()
        sendRestoreState()
    }

    private fun unsubscribeAutomaton() {
        automatonDisposable?.dispose()
        automatonDisposable = null
    }

    private fun maybeDisposeUpdate() {
        updateDisposable?.dispose()
        updateDisposable = null
    }

    // Public

    fun createTag(tag: Tag) = sendCreateTag(tag)

    fun confirm() {
        val checkedTags = automaton.state.value.newExpenseState.tagSelectionState.checkedTags
        sendSetSelectedTags(checkedTags.toList())
    }

    // Sending inputs

    private fun sendLoadTags() = automaton.send(LoadTagsInput)

    private fun sendCreateTag(tag: Tag) = automaton.send(CreateTagInput(
            tag))

    private fun sendCheckTag(tag: Tag) = automaton.send(CheckTagInput(tag))

    private fun sendUncheckTag(tag: Tag) = automaton.send(UncheckTagInput(tag))

    private fun sendDeleteTag(tag: Tag) = automaton.send(DeleteTagInput(
            tag))

    private fun sendSetSelectedTags(selectedTags: List<Tag>) =
            automaton.send(SetSelectedTagsInput(selectedTags))

    private fun sendRestoreState() = automaton.send(RestoreStateInput)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val automaton = application.automaton
            return TagSelectionFragmentModel(automaton) as T
        }
    }
}
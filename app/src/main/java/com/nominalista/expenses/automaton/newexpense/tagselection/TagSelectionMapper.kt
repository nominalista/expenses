package com.nominalista.expenses.automaton.newexpense.tagselection

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeInput
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionInput.*
import com.nominalista.expenses.automaton.ApplicationOutput
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.data.database.DatabaseDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

typealias TagSelectionMapperResult = Pair<TagSelectionState, ApplicationOutput?>

class TagSelectionMapper(private val databaseDataSource: DatabaseDataSource) {

    fun map(state: TagSelectionState, input: TagSelectionInput): TagSelectionMapperResult {
        return when (input) {
            is SetTagsInput -> setTags(state, input)
            is LoadTagsInput -> loadTags(state)
            is CreateTagInput -> createTag(state, input)
            is DeleteTagInput -> deleteTag(state, input)
            is CheckTagInput -> checkTag(state, input)
            is UncheckTagInput -> uncheckTag(state, input)
            is RestoreStateInput -> restoreState()
        }
    }

    private fun setTags(state: TagSelectionState, input: SetTagsInput) =
            TagSelectionMapperResult(state.copy(tags = input.tags), empty())

    private fun loadTags(state: TagSelectionState) =
            TagSelectionMapperResult(state, loadTagsFromDatabase().map { SetTagsInput(it) })

    private fun loadTagsFromDatabase(): Observable<List<Tag>> {
        return databaseDataSource.getTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun createTag(
            state: TagSelectionState,
            input: CreateTagInput
    ): TagSelectionMapperResult {
        val loadTags = Observable.just(LoadTagsInput as ApplicationInput)
        val loadHomeTags = Observable.just(HomeInput.LoadTagsInput as ApplicationInput)
        val output = insertTagIntoDatabase(input.tag)
                .flatMap { Observable.merge(loadTags, loadHomeTags) }
        return TagSelectionMapperResult(state, output)
    }

    private fun insertTagIntoDatabase(tag: Tag): Observable<Long> {
        return databaseDataSource.insertTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun deleteTag(
            state: TagSelectionState,
            input: DeleteTagInput
    ): TagSelectionMapperResult {
        val loadTags = Observable.just(LoadTagsInput as ApplicationInput)
        val loadHomeTags = Observable.just(HomeInput.LoadTagsInput as ApplicationInput)
        val output = deleteTagFromDatabase(input.tag)
                .andThen(Observable.merge(loadTags, loadHomeTags))
        return TagSelectionMapperResult(state, output)
    }

    private fun deleteTagFromDatabase(tag: Tag): Completable {
        return databaseDataSource.deleteTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun checkTag(
            state: TagSelectionState,
            input: CheckTagInput
    ): TagSelectionMapperResult {
        val checkedTags = HashSet(state.checkedTags)
        checkedTags.add(input.tag)
        val newState = state.copy(checkedTags = checkedTags)
        return TagSelectionMapperResult(newState, empty())
    }

    private fun uncheckTag(
            state: TagSelectionState,
            input: UncheckTagInput
    ): TagSelectionMapperResult {
        val checkedTags = HashSet(state.checkedTags)
        checkedTags.remove(input.tag)
        val newState = state.copy(checkedTags = checkedTags)
        return TagSelectionMapperResult(newState, empty())
    }

    private fun restoreState() = TagSelectionMapperResult(TagSelectionState.INITIAL, empty())
}
package com.nominalista.expenses.automaton.newexpense.tagselection

import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.home.HomeInputs
import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionInputs.*
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

    fun map(state: TagSelectionState, input: ApplicationInput): TagSelectionMapperResult {
        return when (input) {
            is SetTagsInput -> setTags(state, input)
            is LoadTagsInput -> loadTags(state)
            is CreateTagInput -> createTag(state, input)
            is DeleteTagInput -> deleteTag(state, input)
            is CheckTagInput -> checkTag(state, input)
            is UncheckTagInput -> uncheckTag(state, input)
            is RestoreStateInput -> restoreState()
            else -> TagSelectionMapperResult(
                    state,
                    null)
        }
    }

    private fun setTags(
            oldState: TagSelectionState,
            input: SetTagsInput
    ): TagSelectionMapperResult {
        val newState = TagSelectionState(
                input.tags,
                oldState.checkedTags)
        return TagSelectionMapperResult(
                newState,
                empty())
    }

    private fun loadTags(oldState: TagSelectionState): TagSelectionMapperResult {
        val output: ApplicationOutput = loadTagsFromDatabase().map { SetTagsInput(it) }
        return TagSelectionMapperResult(
                oldState,
                output)
    }

    private fun loadTagsFromDatabase(): Observable<List<Tag>> {
        return databaseDataSource.getTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun createTag(
            oldState: TagSelectionState,
            input: CreateTagInput
    ): TagSelectionMapperResult {
        val loadTags = Observable.just(LoadTagsInput as ApplicationInput)
        val loadHomeTags = Observable.just(HomeInputs.LoadTagsInput as ApplicationInput)
        val output: ApplicationOutput = insertTagIntoDatabase(input.tag)
                .flatMap { Observable.merge(loadTags, loadHomeTags) }
        return TagSelectionMapperResult(
                oldState,
                output)
    }

    private fun insertTagIntoDatabase(tag: Tag): Observable<Long> {
        return databaseDataSource.insertTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun deleteTag(
            oldState: TagSelectionState,
            input: DeleteTagInput
    ): TagSelectionMapperResult {
        val loadTags = Observable.just(LoadTagsInput as ApplicationInput)
        val loadHomeTags = Observable.just(HomeInputs.LoadTagsInput as ApplicationInput)
        val output = deleteTagFromDatabase(input.tag)
                .andThen(Observable.merge(loadTags, loadHomeTags))
        return TagSelectionMapperResult(
                oldState,
                output)
    }

    private fun deleteTagFromDatabase(tag: Tag): Completable {
        return databaseDataSource.deleteTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun checkTag(
            oldState: TagSelectionState,
            input: CheckTagInput
    ): TagSelectionMapperResult {
        val checkedTags = HashSet(oldState.checkedTags)
        checkedTags.add(input.tag)
        val newState = TagSelectionState(oldState.tags, checkedTags)
        return TagSelectionMapperResult(newState, empty())
    }

    private fun uncheckTag(
            oldState: TagSelectionState,
            input: UncheckTagInput
    ): TagSelectionMapperResult {
        val checkedTags = HashSet(oldState.checkedTags)
        checkedTags.remove(input.tag)
        val newState = TagSelectionState(oldState.tags, checkedTags)
        return TagSelectionMapperResult(newState, empty())
    }

    private fun restoreState(): TagSelectionMapperResult {
        return TagSelectionMapperResult(
                TagSelectionState.INITIAL,
                empty())
    }
}
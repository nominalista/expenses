package com.nominalista.expenses.automaton.newexpense.tagselection

import com.nominalista.expenses.automaton.newexpense.NewExpenseInput
import com.nominalista.expenses.data.Tag

sealed class TagSelectionInput: NewExpenseInput() {

    object LoadTagsInput : TagSelectionInput()

    data class SetTagsInput(val tags: List<Tag>) : TagSelectionInput()

    data class CreateTagInput(val tag: Tag) : TagSelectionInput()

    data class DeleteTagInput(val tag: Tag) : TagSelectionInput()

    data class CheckTagInput(val tag: Tag) : TagSelectionInput()

    data class UncheckTagInput(val tag: Tag) : TagSelectionInput()

    object RestoreStateInput : TagSelectionInput()
}
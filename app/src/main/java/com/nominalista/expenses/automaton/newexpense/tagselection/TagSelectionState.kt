package com.nominalista.expenses.automaton.newexpense.tagselection

import com.nominalista.expenses.data.Tag

data class TagSelectionState(
        val tags: List<Tag>,
        val checkedTags: HashSet<Tag>
) {
    companion object {
        val INITIAL = TagSelectionState(ArrayList(), HashSet())
    }
}
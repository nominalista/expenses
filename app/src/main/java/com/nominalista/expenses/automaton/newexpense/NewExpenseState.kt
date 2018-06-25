package com.nominalista.expenses.automaton.newexpense

import com.nominalista.expenses.automaton.newexpense.tagselection.TagSelectionState
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Tag

data class NewExpenseState(
        val selectedCurrency: Currency,
        val selectedDate: Date,
        val selectedTags: List<Tag>,
        val amount: Float,
        val title: String,
        val notes: String,
        val tagSelectionState: TagSelectionState
) {
    companion object {
        val INITIAL = NewExpenseState(Currency.USD,
                Date.now(),
                ArrayList(),
                0f,
                "",
                "",
                TagSelectionState.INITIAL)
    }
}
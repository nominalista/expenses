package com.nominalista.expenses.automaton.newexpense

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.data.Currency
import com.nominalista.expenses.data.Date
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag

interface NewExpenseInputs {

    data class LoadDefaultCurrencyInput(val context: Context) : ApplicationInput

    data class SetSelectedCurrencyInput(val selectedCurrency: Currency) : ApplicationInput

    data class SetSelectedDateInput(val selectedDate: Date) : ApplicationInput

    data class SetSelectedTagsInput(val selectedTags: List<Tag>) : ApplicationInput

    data class SetAmountInput(val amount: Float) : ApplicationInput

    data class SetTitleInput(val title: String) : ApplicationInput

    data class SetNotesInput(val notes: String) : ApplicationInput

    data class CreateExpenseInput(val expense: Expense) : ApplicationInput

    object RestoreStateInput : ApplicationInput
}
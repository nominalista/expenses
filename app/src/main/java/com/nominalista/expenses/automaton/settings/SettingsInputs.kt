package com.nominalista.expenses.automaton.settings

import android.content.Context
import com.nominalista.expenses.automaton.ApplicationInput
import com.nominalista.expenses.automaton.settings.SettingsState.ExpenseExportState
import com.nominalista.expenses.data.Currency

interface SettingsInputs {

    data class LoadDefaultCurrencyInput(val context: Context) : ApplicationInput

    data class SetDefaultCurrencyInput(val defaultCurrency: Currency?) : ApplicationInput

    data class SaveDefaultCurrencyInput(
            val context: Context,
            val defaultCurrency: Currency
    ) : ApplicationInput

    data class ExportExpensesInput(val context: Context) : ApplicationInput

    data class SetExpenseExportStateInput(val expenseExportState: ExpenseExportState)
        : ApplicationInput

    object DeleteAllExpensesInput : ApplicationInput

    object RestoreStateInput : ApplicationInput
}
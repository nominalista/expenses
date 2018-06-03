package com.nominalista.expenses.ui.settings

import android.view.View

class DeleteAllExpensesItemHolder(itemView: View): SettingItemHolder(itemView) {

    fun bind(model: DeleteAllExpensesItemModel) {
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
    }
}
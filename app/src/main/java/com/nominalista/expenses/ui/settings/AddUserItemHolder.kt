package com.nominalista.expenses.ui.settings

import android.view.View
import kotlinx.android.synthetic.main.item_add_user.view.*

class AddUserItemHolder(itemView: View) : SettingItemHolder(itemView) {

    fun bind(viewModel: AddUserItemModel) {
        itemView.setOnClickListener { viewModel.click?.invoke() }
        itemView.button_add.setOnClickListener { viewModel.click?.invoke() }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
        itemView.button_add.setOnClickListener(null)
    }
}
package com.nominalista.expenses.ui.settings

import android.view.View
import kotlinx.android.synthetic.main.item_user.view.*

class UserItemHolder(itemView: View) : SettingItemHolder(itemView) {

    fun bind(viewModel: UserItemModel) {
        itemView.text_name.text = viewModel.name
        itemView.button_delete.setOnClickListener { viewModel.deleteButtonClick?.invoke() }
    }

    fun recycle() {
        itemView.text_name.text = ""
        itemView.button_delete.setOnClickListener(null)
    }
}
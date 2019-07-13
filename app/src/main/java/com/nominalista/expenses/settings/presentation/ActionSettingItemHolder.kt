package com.nominalista.expenses.settings.presentation

import android.view.View
import kotlinx.android.synthetic.main.item_action_setting.view.*

open class ActionSettingItemHolder(itemView: View): SettingItemHolder(itemView) {

    fun bind(model: ActionSettingItemModel) {
        itemView.text_title.text = model.title
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.text_title.text = null
        itemView.setOnClickListener(null)
    }
}
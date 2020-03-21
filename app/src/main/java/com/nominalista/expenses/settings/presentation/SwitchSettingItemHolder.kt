package com.nominalista.expenses.settings.presentation

import android.view.View
import kotlinx.android.synthetic.main.item_switch_setting.view.*

class SwitchSettingItemHolder(itemView: View) : SettingItemHolder(itemView) {
    fun bind(model: SwitchSettingItemModel) {
        itemView.switch_item.text = model.title
        itemView.switch_item.isChecked = model.enabled
        itemView.switch_item.setOnCheckedChangeListener { _, isChecked ->
            model.isChecked?.invoke(isChecked)
        }
    }

    fun recycle() {
        itemView.switch_item.text = null
        itemView.switch_item.setOnCheckedChangeListener(null)
    }
}

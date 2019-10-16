package com.nominalista.expenses.settings.presentation

import android.view.View
import kotlinx.android.synthetic.main.header_settings.view.*

class SettingsHeaderHolder(itemView: View) : SettingItemHolder(itemView) {

    fun bind(model: SettingsHeaderModel) {
        itemView.text_title.text = model.title
    }

    fun recycle() {
        itemView.text_title.text = null
    }
}
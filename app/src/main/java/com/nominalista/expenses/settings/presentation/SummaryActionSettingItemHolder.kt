package com.nominalista.expenses.settings.presentation

import android.view.View
import kotlinx.android.synthetic.main.item_summary_action_setting.view.*

class SummaryActionSettingItemHolder(itemView: View): SettingItemHolder(itemView) {

    fun bind(model: SummaryActionSettingItemModel) {
        itemView.text_title.text = model.title
        itemView.text_summary.text = model.summary
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.text_title.text = null
        itemView.text_summary.text = null
        itemView.setOnClickListener(null)
    }
}
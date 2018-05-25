package com.nominalista.expenses.ui.settings

import android.view.View
import kotlinx.android.synthetic.main.item_default_currency.view.*

class DefaultCurrencyItemHolder(itemView: View) : SettingItemHolder(itemView) {

    fun bind(itemModel: DefaultCurrencyItemModel) {
        itemView.text_flag.text = itemModel.flag
        itemView.text_subtitle.text = itemModel.subtitle
        itemView.setOnClickListener { itemModel.click?.invoke() }
    }

    fun recycle() {
        itemView.text_flag.text = ""
        itemView.text_subtitle.text = ""
        itemView.setOnClickListener(null)
    }
}
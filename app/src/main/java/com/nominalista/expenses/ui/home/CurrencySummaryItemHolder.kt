package com.nominalista.expenses.ui.home

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_currency_summary.view.*

class CurrencySummaryItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(itemModel: CurrencySummaryItemModel) {
        itemView.text_amount.text = itemModel.amount
        itemView.text_symbol.text = itemModel.currency
    }

    fun recycle() {
        itemView.text_amount.text = ""
        itemView.text_symbol.text = ""
    }
}
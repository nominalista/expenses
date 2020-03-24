package com.nominalista.expenses.home.presentation

import android.view.View
import kotlinx.android.synthetic.main.item_expense.view.*

class ExpenseItemHolder(itemView: View) : HomeItemHolder(itemView) {

    fun bind(model: ExpenseItemModel) {
        itemView.text_month.text = model.month
        itemView.text_day.text = model.day
        itemView.text_amount.text = model.amount
        itemView.text_title.text = model.title
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.text_month.text = ""
        itemView.text_day.text = ""
        itemView.text_amount.text = ""
        itemView.text_title.text = ""
        itemView.setOnClickListener(null)
    }
}
package com.nominalista.expenses.sms.rule

import android.view.View

class AddRuleItemHolder(itemView: View) : RuleItemHolder(itemView) {
    fun bind(model: AddRuleItemModel) {
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
    }
}

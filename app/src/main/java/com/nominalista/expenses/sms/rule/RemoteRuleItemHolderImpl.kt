package com.nominalista.expenses.sms.rule

import android.view.View
import kotlinx.android.synthetic.main.item_remote_rule.view.*

class RemoteRuleItemHolderImpl(itemView: View) : RuleItemHolder(itemView) {
    fun bind(model: RemoteRuleItemModelImpl) {
        itemView.text_name.text = model.name.first()
        itemView.button_more.setOnClickListener { model.useClick?.invoke() }
    }

    fun recycle() {
        itemView.text_name.text = ""
        itemView.button_more.setOnClickListener(null)
    }
}

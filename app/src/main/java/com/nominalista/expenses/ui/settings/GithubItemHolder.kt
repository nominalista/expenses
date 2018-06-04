package com.nominalista.expenses.ui.settings

import android.view.View

class GithubItemHolder(itemView: View): SettingItemHolder(itemView) {

    fun bind(model: GithubItemModel) {
        itemView.setOnClickListener { model.click?.invoke() }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
    }
}
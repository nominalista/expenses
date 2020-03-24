package com.nominalista.expenses.home.presentation

import android.view.View
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_tag_filter.view.*

class TagFilterItemHolder(itemView: View) : HomeItemHolder(itemView) {

    fun bind(model: TagFilterItemModel) {
        model.chips.forEach { itemView.chip_group.addView(createChip(it)) }
        itemView.button_clear.setOnClickListener { model.clearClick?.invoke() }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(itemView.context)
        chip.text = text
        chip.isClickable = false
        return chip
    }

    fun recycle() {
        itemView.chip_group.removeAllViews()
        itemView.button_clear.setOnClickListener(null)
    }
}
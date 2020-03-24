package com.nominalista.expenses.home.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

class SummaryAdapter
    : ListAdapter<CurrencySummaryItemModel, CurrencySummaryItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencySummaryItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_currency_summary, parent, false)
        return CurrencySummaryItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: CurrencySummaryItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: CurrencySummaryItemHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    private class DiffCallback : DiffUtil.ItemCallback<CurrencySummaryItemModel>() {

        override fun areItemsTheSame(
                oldItem: CurrencySummaryItemModel,
                newItem: CurrencySummaryItemModel
        ) = oldItem == newItem

        override fun areContentsTheSame(
                oldItem: CurrencySummaryItemModel,
                newItem: CurrencySummaryItemModel
        ) = oldItem == newItem
    }
}
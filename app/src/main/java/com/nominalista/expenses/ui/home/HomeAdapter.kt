package com.nominalista.expenses.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

private const val EXPENSE_ITEM_TYPE = R.layout.item_expense
private const val SUMMARY_ITEM_TYPE = R.layout.item_summary

class HomeAdapter : ListAdapter<HomeItemModel, HomeItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            EXPENSE_ITEM_TYPE -> ExpenseItemHolder(itemView)
            SUMMARY_ITEM_TYPE -> SummaryItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: HomeItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            (holder is ExpenseItemHolder && itemModel is ExpenseItemModel) -> holder.bind(itemModel)
            (holder is SummaryItemHolder && itemModel is SummaryItemModel) -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: HomeItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ExpenseItemHolder -> holder.recycle()
            is SummaryItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ExpenseItemModel -> EXPENSE_ITEM_TYPE
            is SummaryItemModel -> SUMMARY_ITEM_TYPE
            else -> super.getItemViewType(position)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<HomeItemModel>() {

        override fun areItemsTheSame(
                oldItem: HomeItemModel,
                newItem: HomeItemModel
        ): Boolean {
            return when {
                (oldItem is ExpenseItemModel && newItem is ExpenseItemModel) ->
                    oldItem.expense.id == newItem.expense.id
                (oldItem is SummaryItemModel && newItem is SummaryItemModel) -> true
                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: HomeItemModel,
                newItem: HomeItemModel
        ): Boolean {
            return when {
                (oldItem is ExpenseItemModel && newItem is ExpenseItemModel) ->
                    oldItem.expense == newItem.expense
                (oldItem is SummaryItemModel && newItem is SummaryItemModel) -> false
                else -> false
            }
        }
    }
}
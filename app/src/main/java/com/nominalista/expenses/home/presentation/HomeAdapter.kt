package com.nominalista.expenses.home.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

class HomeAdapter : ListAdapter<HomeItemModel, HomeItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            EXPENSE_ITEM_TYPE -> ExpenseItemHolder(itemView)
            SUMMARY_ITEM_TYPE -> SummaryItemHolder(itemView)
            TAG_FILTER_ITEM_TYPE -> TagFilterItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: HomeItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            (holder is ExpenseItemHolder && itemModel is ExpenseItemModel) -> holder.bind(itemModel)
            (holder is SummaryItemHolder && itemModel is SummaryItemModel) -> holder.bind(itemModel)
            (holder is TagFilterItemHolder && itemModel is TagFilterItemModel) ->
                holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: HomeItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ExpenseItemHolder -> holder.recycle()
            is SummaryItemHolder -> holder.recycle()
            is TagFilterItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ExpenseItemModel -> EXPENSE_ITEM_TYPE
            is SummaryItemModel -> SUMMARY_ITEM_TYPE
            is TagFilterItemModel -> TAG_FILTER_ITEM_TYPE
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
                (oldItem is TagFilterItemModel && newItem is TagFilterItemModel) -> true
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
                (oldItem is SummaryItemModel && newItem is SummaryItemModel) ->
                    areContentsOfSummaryItemModelsTheSame(oldItem, newItem)
                (oldItem is TagFilterItemModel && newItem is TagFilterItemModel) ->
                    oldItem.tagFilter == newItem.tagFilter
                else -> false
            }
        }

        private fun areContentsOfSummaryItemModelsTheSame(
                oldItem: SummaryItemModel,
                newItem: SummaryItemModel
        ): Boolean {
            return oldItem.currencySummaries == newItem.currencySummaries
                    && oldItem.dateRange == newItem.dateRange
        }
    }

    companion object {

        private const val EXPENSE_ITEM_TYPE = R.layout.item_expense
        private const val SUMMARY_ITEM_TYPE = R.layout.item_summary
        private const val TAG_FILTER_ITEM_TYPE = R.layout.item_tag_filter
    }
}
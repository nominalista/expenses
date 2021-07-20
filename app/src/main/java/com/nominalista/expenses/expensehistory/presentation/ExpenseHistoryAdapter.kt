package com.nominalista.expenses.expensehistory.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R
import com.nominalista.expenses.home.presentation.ExpenseItemHolder
import com.nominalista.expenses.home.presentation.ExpenseItemModel
import com.nominalista.expenses.home.presentation.HomeItemHolder
import com.nominalista.expenses.home.presentation.HomeItemModel

class ExpenseHistoryAdapter : ListAdapter<HomeItemModel, HomeItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            EXPENSE_ITEM_TYPE -> ExpenseItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: HomeItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            (holder is ExpenseItemHolder && itemModel is ExpenseItemModel) -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: HomeItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ExpenseItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ExpenseItemModel -> EXPENSE_ITEM_TYPE
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
                else -> false
            }
        }
    }

    companion object {

        private const val EXPENSE_ITEM_TYPE = R.layout.item_expense
    }
}
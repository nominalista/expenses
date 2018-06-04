package com.nominalista.expenses.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

private const val DEFAULT_CURRENCY_ITEM_TYPE = R.layout.item_default_currency
private const val DELETE_ALL_EXPENSES_ITEM_TYPE = R.layout.item_delete_all_expenses
private const val EXPENSE_HEADER_TYPE = R.layout.header_expense
private const val GENERAL_HEADER_TYPE = R.layout.header_general
private const val GITHUB_ITEM_TYPE = R.layout.item_github

class SettingsAdapter : ListAdapter<SettingItemModel, SettingItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            DEFAULT_CURRENCY_ITEM_TYPE -> DefaultCurrencyItemHolder(itemView)
            DELETE_ALL_EXPENSES_ITEM_TYPE -> DeleteAllExpensesItemHolder(itemView)
            EXPENSE_HEADER_TYPE -> ExpenseHeaderHolder(itemView)
            GENERAL_HEADER_TYPE -> ExpenseHeaderHolder(itemView)
            GITHUB_ITEM_TYPE -> GithubItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: SettingItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            holder is DefaultCurrencyItemHolder && itemModel is DefaultCurrencyItemModel ->
                holder.bind(itemModel)
            holder is DeleteAllExpensesItemHolder && itemModel is DeleteAllExpensesItemModel ->
                holder.bind(itemModel)
            holder is GithubItemHolder && itemModel is GithubItemModel -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: SettingItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is DefaultCurrencyItemHolder -> holder.recycle()
            is DeleteAllExpensesItemHolder -> holder.recycle()
            is GithubItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DefaultCurrencyItemModel -> DEFAULT_CURRENCY_ITEM_TYPE
            is DeleteAllExpensesItemModel -> DELETE_ALL_EXPENSES_ITEM_TYPE
            is ExpenseHeaderModel -> EXPENSE_HEADER_TYPE
            is GeneralHeaderModel -> GENERAL_HEADER_TYPE
            is GithubItemModel -> GITHUB_ITEM_TYPE
            else -> super.getItemViewType(position)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SettingItemModel>() {

        override fun areItemsTheSame(
                oldItem: SettingItemModel,
                newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is DefaultCurrencyItemModel && newItem is DefaultCurrencyItemModel ->
                    oldItem.currency.code == newItem.currency.code
                oldItem is DeleteAllExpensesItemModel && newItem is DeleteAllExpensesItemModel ->
                    true
                oldItem is ExpenseHeaderModel && newItem is ExpenseHeaderModel -> true
                oldItem is GeneralHeaderModel && newItem is GeneralHeaderModel -> true
                oldItem is GithubItemModel && newItem is GithubItemModel -> true
                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: SettingItemModel,
                newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is DefaultCurrencyItemModel && newItem is DefaultCurrencyItemModel ->
                    oldItem.currency == newItem.currency
                oldItem is DeleteAllExpensesItemModel && newItem is DeleteAllExpensesItemModel ->
                    true
                oldItem is ExpenseHeaderModel && newItem is ExpenseHeaderModel -> true
                oldItem is GeneralHeaderModel && newItem is GeneralHeaderModel -> true
                oldItem is GithubItemModel && newItem is GithubItemModel -> true
                else -> false
            }
        }
    }
}
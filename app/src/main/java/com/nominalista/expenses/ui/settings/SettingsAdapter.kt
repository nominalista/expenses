package com.nominalista.expenses.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

private const val ADD_USER_ITEM_TYPE = R.layout.item_add_user
private const val DEFAULT_CURRENCY_ITEM_TYPE = R.layout.item_default_currency
private const val OTHER_HEADER_TYPE = R.layout.header_other
private const val USER_HEADER_TYPE = R.layout.header_user
private const val USER_ITEM_TYPE = R.layout.item_user

class SettingsAdapter : ListAdapter<SettingItemModel, SettingItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            ADD_USER_ITEM_TYPE -> AddUserItemHolder(itemView)
            DEFAULT_CURRENCY_ITEM_TYPE -> DefaultCurrencyItemHolder(itemView)
            OTHER_HEADER_TYPE -> OtherHeaderHolder(itemView)
            USER_HEADER_TYPE -> UserHeaderHolder(itemView)
            USER_ITEM_TYPE -> UserItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: SettingItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            holder is AddUserItemHolder && itemModel is AddUserItemModel -> holder.bind(itemModel)
            holder is DefaultCurrencyItemHolder && itemModel is DefaultCurrencyItemModel ->
                holder.bind(itemModel)
            holder is UserItemHolder && itemModel is UserItemModel -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: SettingItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is AddUserItemHolder -> holder.recycle()
            is DefaultCurrencyItemHolder -> holder.recycle()
            is UserItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AddUserItemModel -> ADD_USER_ITEM_TYPE
            is DefaultCurrencyItemModel -> DEFAULT_CURRENCY_ITEM_TYPE
            is OtherHeaderModel -> OTHER_HEADER_TYPE
            is UserItemModel -> USER_ITEM_TYPE
            is UserHeaderModel -> USER_HEADER_TYPE
            else -> super.getItemViewType(position)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SettingItemModel>() {

        override fun areItemsTheSame(
                oldItem: SettingItemModel,
                newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is AddUserItemModel && newItem is AddUserItemModel -> true
                oldItem is DefaultCurrencyItemModel && newItem is DefaultCurrencyItemModel ->
                    oldItem.currency.code == newItem.currency.code
                oldItem is OtherHeaderModel && newItem is OtherHeaderModel -> true
                oldItem is UserHeaderModel && newItem is UserHeaderModel -> true
                oldItem is UserItemModel && newItem is UserItemModel ->
                    oldItem.user.name == newItem.user.name
                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: SettingItemModel,
                newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is AddUserItemModel && newItem is AddUserItemModel -> true
                oldItem is DefaultCurrencyItemModel && newItem is DefaultCurrencyItemModel ->
                    oldItem.currency == newItem.currency
                oldItem is OtherHeaderModel && newItem is OtherHeaderModel -> true
                oldItem is UserHeaderModel && newItem is UserHeaderModel -> true
                oldItem is UserItemModel && newItem is UserItemModel -> oldItem.user == newItem.user
                else -> false
            }
        }
    }
}
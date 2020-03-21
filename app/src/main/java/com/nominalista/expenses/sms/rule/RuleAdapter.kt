package com.nominalista.expenses.sms.rule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

class RuleAdapter : ListAdapter<RuleItemModel, RuleItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            ADD_TAG_ITEM_TYPE -> AddRuleItemHolder(itemView)
            TAG_ITEM_TYPE -> RuleItemHolderImpl(itemView)
            REMOTE_TAG_ITEM_TYPE -> RemoteRuleItemHolderImpl(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RuleItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            holder is AddRuleItemHolder && itemModel is AddRuleItemModel -> holder.bind(itemModel)
            holder is RuleItemHolderImpl && itemModel is RuleItemModelImpl -> holder.bind(itemModel)
            holder is RemoteRuleItemHolderImpl && itemModel is RemoteRuleItemModelImpl -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: RuleItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is AddRuleItemHolder -> holder.recycle()
            is RuleItemHolderImpl -> holder.recycle()
            is RemoteRuleItemHolderImpl -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AddRuleItemModel -> ADD_TAG_ITEM_TYPE
            is RuleItemModelImpl -> TAG_ITEM_TYPE
            is RemoteRuleItemModelImpl -> REMOTE_TAG_ITEM_TYPE
            else -> throw IllegalArgumentException()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RuleItemModel>() {

        override fun areItemsTheSame(
                oldItem: RuleItemModel,
                newItem: RuleItemModel
        ): Boolean {
            return when {
                oldItem is AddRuleItemModel && newItem is AddRuleItemModel -> true
                oldItem is RuleItemModelImpl && newItem is RuleItemModelImpl -> oldItem.rule.id == newItem.rule.id
                oldItem is RemoteRuleItemModelImpl && newItem is RemoteRuleItemModelImpl -> oldItem.rule.id == newItem.rule.id
                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: RuleItemModel,
                newItem: RuleItemModel
        ): Boolean {
            return when {
                oldItem is AddRuleItemModel && newItem is AddRuleItemModel -> true
                oldItem is RuleItemModelImpl && newItem is RuleItemModelImpl -> oldItem.rule == newItem.rule
                oldItem is RemoteRuleItemModelImpl && newItem is RemoteRuleItemModelImpl -> oldItem.rule == newItem.rule
                else -> false
            }
        }
    }

    companion object {
        private const val ADD_TAG_ITEM_TYPE = R.layout.item_add_rule
        private const val TAG_ITEM_TYPE = R.layout.item_rule
        private const val REMOTE_TAG_ITEM_TYPE = R.layout.item_remote_rule
    }
}
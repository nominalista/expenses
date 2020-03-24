package com.nominalista.expenses.settings.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

class SettingsAdapter : ListAdapter<SettingItemModel, SettingItemHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            ACTION_SETTING_ITEM_TYPE -> ActionSettingItemHolder(itemView)
            SUMMARY_ACTION_SETTING_ITEM_TYPE -> SummaryActionSettingItemHolder(
                itemView
            )
            SETTINGS_HEADER_TYPE -> SettingsHeaderHolder(
                itemView
            )
            SWITCH_SETTING_ITEM_TYPE -> SwitchSettingItemHolder(itemView)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: SettingItemHolder, position: Int) {
        val model = getItem(position)
        when {
            holder is ActionSettingItemHolder && model is ActionSettingItemModel ->
                holder.bind(model)
            holder is SummaryActionSettingItemHolder && model is SummaryActionSettingItemModel ->
                holder.bind(model)
            holder is SettingsHeaderHolder && model is SettingsHeaderModel ->
                holder.bind(model)
            holder is SwitchSettingItemHolder && model is SwitchSettingItemModel ->
                holder.bind(model)
        }
    }

    override fun onViewRecycled(holder: SettingItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ActionSettingItemHolder -> holder.recycle()
            is SummaryActionSettingItemHolder -> holder.recycle()
            is SettingsHeaderHolder -> holder.recycle()
            is SwitchSettingItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ActionSettingItemModel -> ACTION_SETTING_ITEM_TYPE
            is SummaryActionSettingItemModel -> SUMMARY_ACTION_SETTING_ITEM_TYPE
            is SettingsHeaderModel -> SETTINGS_HEADER_TYPE
            is SwitchSettingItemModel -> SWITCH_SETTING_ITEM_TYPE
            else -> super.getItemViewType(position)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SettingItemModel>() {

        override fun areItemsTheSame(
            oldItem: SettingItemModel,
            newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is ActionSettingItemModel && newItem is ActionSettingItemModel ->
                    oldItem.title == newItem.title
                oldItem is SummaryActionSettingItemModel && newItem is SummaryActionSettingItemModel ->
                    oldItem.title == newItem.title && oldItem.summary == newItem.summary
                oldItem is SettingsHeaderModel && newItem is SettingsHeaderModel ->
                    oldItem.title == newItem.title
                oldItem is SwitchSettingItemModel && newItem is SwitchSettingItemModel ->
                    oldItem.title == newItem.title
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: SettingItemModel,
            newItem: SettingItemModel
        ): Boolean {
            return when {
                oldItem is ActionSettingItemModel && newItem is ActionSettingItemModel ->
                    oldItem.title == newItem.title
                oldItem is SummaryActionSettingItemModel && newItem is SummaryActionSettingItemModel ->
                    oldItem.title == newItem.title && oldItem.summary == newItem.summary
                oldItem is SettingsHeaderModel && newItem is SettingsHeaderModel ->
                    oldItem.title == newItem.title
                oldItem is SwitchSettingItemModel && newItem is SwitchSettingItemModel ->
                    oldItem.title == newItem.title
                else -> false
            }
        }
    }

    companion object {
        private const val ACTION_SETTING_ITEM_TYPE = R.layout.item_action_setting
        private const val SUMMARY_ACTION_SETTING_ITEM_TYPE = R.layout.item_summary_action_setting
        private const val SETTINGS_HEADER_TYPE = R.layout.header_settings
        private const val SWITCH_SETTING_ITEM_TYPE = R.layout.item_switch_setting
    }
}
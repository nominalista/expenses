package com.nominalista.expenses.addeditexpense.presentation.tagselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nominalista.expenses.R

class TagSelectionAdapter
    : ListAdapter<TagSelectionItemModel, TagSelectionItemHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagSelectionItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            ADD_TAG_ITEM_TYPE -> AddTagItemHolder(
                itemView
            )
            TAG_ITEM_TYPE -> TagItemHolder(
                itemView
            )
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: TagSelectionItemHolder, position: Int) {
        val itemModel = getItem(position)
        when {
            holder is AddTagItemHolder && itemModel is AddTagItemModel -> holder.bind(itemModel)
            holder is TagItemHolder && itemModel is TagItemModel -> holder.bind(itemModel)
        }
    }

    override fun onViewRecycled(holder: TagSelectionItemHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is AddTagItemHolder -> holder.recycle()
            is TagItemHolder -> holder.recycle()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AddTagItemModel -> ADD_TAG_ITEM_TYPE
            is TagItemModel -> TAG_ITEM_TYPE
            else -> throw IllegalArgumentException()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TagSelectionItemModel>() {

        override fun areItemsTheSame(
            oldItem: TagSelectionItemModel,
            newItem: TagSelectionItemModel
        ): Boolean {
            return when {
                oldItem is AddTagItemModel && newItem is AddTagItemModel -> true
                oldItem is TagItemModel && newItem is TagItemModel ->
                    oldItem.tag.id == newItem.tag.id
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: TagSelectionItemModel,
            newItem: TagSelectionItemModel
        ): Boolean {
            return when {
                oldItem is AddTagItemModel && newItem is AddTagItemModel -> true
                oldItem is TagItemModel && newItem is TagItemModel ->
                    oldItem.tag == newItem.tag
                else -> false
            }
        }
    }

    companion object {
        private const val ADD_TAG_ITEM_TYPE = R.layout.item_add_tag
        private const val TAG_ITEM_TYPE = R.layout.item_tag
    }
}
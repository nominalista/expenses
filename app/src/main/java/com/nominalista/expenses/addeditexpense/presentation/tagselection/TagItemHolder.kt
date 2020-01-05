package com.nominalista.expenses.addeditexpense.presentation.tagselection

import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.nominalista.expenses.R
import kotlinx.android.synthetic.main.item_tag.view.*

class TagItemHolder(itemView: View): TagSelectionItemHolder(itemView) {

    fun bind(model: TagItemModel) {
        itemView.text_name.text = model.name
        itemView.check_box.isChecked = model.isChecked
        itemView.check_box.setOnClickListener { model.checkClick?.invoke() }
        itemView.button_more.setOnClickListener { showMore(model) }
        itemView.setOnClickListener { itemView.check_box.performClick() }
    }

    private fun showMore(model: TagItemModel) {
        val context = itemView.context
        val popupMenu = PopupMenu(context, itemView.button_more)
        popupMenu.menu.add(context.getString(R.string.delete))
        popupMenu.setOnMenuItemClickListener { model.deleteClick?.invoke(); true }
        popupMenu.show()
    }

    fun recycle() {
        itemView.text_name.text = ""
        itemView.check_box.isChecked = false
        itemView.check_box.setOnClickListener(null)
        itemView.button_more.setOnClickListener(null)
        itemView.setOnClickListener(null)
    }
}
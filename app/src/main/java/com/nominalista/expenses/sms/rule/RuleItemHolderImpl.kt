package com.nominalista.expenses.sms.rule

import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.nominalista.expenses.R
import kotlinx.android.synthetic.main.item_tag.view.*

class RuleItemHolderImpl(itemView: View) : RuleItemHolder(itemView) {
    fun bind(model: RuleItemModelImpl) {
        itemView.text_name.text = model.name.first()
        itemView.button_more.setOnClickListener { showMore(model) }
    }

    private fun showMore(model: RuleItemModelImpl) {
        val context = itemView.context
        val popupMenu = PopupMenu(context, itemView.button_more)
        popupMenu.inflate(R.menu.menu_rule)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> delete(model)
                R.id.duplicate -> duplicate(model)
                R.id.use -> use(model)
                else -> edit(model)
            }
        }
        popupMenu.show()
    }

    private fun share(model: RuleItemModelImpl): Boolean {
        model.shareClick?.invoke()
        return true
    }

    private fun duplicate(model: RuleItemModelImpl): Boolean {
        model.duplicateClick?.invoke()
        return true
    }

    private fun edit(model: RuleItemModelImpl): Boolean {
        model.editClick?.invoke()
        return true
    }

    private fun delete(model: RuleItemModelImpl): Boolean {
        model.deleteClick?.invoke()
        return true
    }

    private fun use(model: RuleItemModelImpl): Boolean {
        model.useClick?.invoke()
        return true
    }

    fun recycle() {
        itemView.text_name.text = ""
        itemView.button_more.setOnClickListener(null)
    }
}

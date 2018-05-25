package com.nominalista.expenses.ui.home

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.nominalista.expenses.R
import com.nominalista.expenses.ui.common.widgets.CirclePagerIndicatorDecoration
import kotlinx.android.synthetic.main.item_summary.view.*

class SummaryItemHolder(itemView: View) : HomeItemHolder(itemView) {

    private var adapter = SummaryAdapter()
    private val layoutManager = LinearLayoutManager(itemView.context,
            LinearLayoutManager.HORIZONTAL,
            false)
    private val indicatorDecoration = CirclePagerIndicatorDecoration(itemView.context,
            R.color.icon_active_light,
            R.color.icon_inactive_light)
    private val snapHelper = PagerSnapHelper()

    init {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        itemView.recycler_view.adapter = adapter
        itemView.recycler_view.layoutManager = layoutManager
        itemView.recycler_view.addItemDecoration(indicatorDecoration)
        snapHelper.attachToRecyclerView(itemView.recycler_view)
    }

    fun bind(model: SummaryItemModel) {
        itemView.chip_date_range.chipText = model.dateRangeText
        itemView.chip_date_range.setOnClickListener { showPopupMenu(model) }
        adapter.submitList(model.itemModels)
    }

    private fun showPopupMenu(model: SummaryItemModel) {
        val popupMenu = PopupMenu(itemView.context, itemView.chip_date_range)
        popupMenu.inflate(R.menu.menu_date_range)
        popupMenu.setOnMenuItemClickListener { menuItemSelected(it, model) }
        popupMenu.show()
    }

    private fun menuItemSelected(item: MenuItem, model: SummaryItemModel): Boolean {
        return when (item.itemId) {
            R.id.today -> { model.onTodayClick(); true }
            R.id.this_week -> { model.onThisWeekClick(); true }
            R.id.this_month -> { model.onThisMonthClick(); true }
            R.id.all_time -> { model.onAllTimeClick(); true }
            else -> false
        }
    }

    fun recycle() {
        itemView.chip_date_range.chipText = ""
        itemView.chip_date_range.setOnClickListener(null)
        adapter.submitList(emptyList())
    }
}
package com.nominalista.expenses.ui.home

import android.content.Context
import com.nominalista.expenses.R
import com.nominalista.expenses.model.Currency
import com.nominalista.expenses.model.DateRange
import com.nominalista.expenses.model.DateRange.*

class SummaryItemModel(
        context: Context,
        currencySummaries: List<Pair<Currency, Float>>,
        dateRange: DateRange
) : HomeItemModel {

    var itemModels: List<CurrencySummaryItemModel>
    var dateRangeText: String
    var dateRangeChange: ((DateRange) -> Unit)? = null

    init {
        itemModels = createItemModels(currencySummaries)
        dateRangeText = createDateRangeText(context, dateRange)
    }

    private fun createItemModels(
            currencySummaries: List<Pair<Currency, Float>>
    ): List<CurrencySummaryItemModel> {
        return currencySummaries.map { createCurrencySummaryItemModel(it.first, it.second) }
    }

    private fun createCurrencySummaryItemModel(
            currency: Currency,
            amount: Float
    ): CurrencySummaryItemModel {
        return CurrencySummaryItemModel(currency, amount)
    }

    private fun createDateRangeText(context: Context, dateRange: DateRange): String {
        return when (dateRange) {
            Today -> context.getString(R.string.ui_home_today)
            ThisWeek -> context.getString(R.string.ui_home_this_week)
            ThisMonth -> context.getString(R.string.ui_home_this_month)
            AllTime -> context.getString(R.string.ui_home_all_time)
        }
    }

    fun onTodayClick() = dateRangeChange?.invoke(Today)

    fun onThisWeekClick() = dateRangeChange?.invoke(DateRange.ThisWeek)

    fun onThisMonthClick() = dateRangeChange?.invoke(DateRange.ThisMonth)

    fun onAllTimeClick() = dateRangeChange?.invoke(DateRange.AllTime)
}
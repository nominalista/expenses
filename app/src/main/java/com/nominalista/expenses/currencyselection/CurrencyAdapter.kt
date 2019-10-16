package com.nominalista.expenses.currencyselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Currency
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_currency.*

class CurrencyAdapter(private val listener: Listener) :
    ListAdapter<Currency, CurrencyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_currency, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = getItem(position)
        holder.textFlag.text = currency.flag
        holder.textCode.text = currency.code
        holder.textTitle.text = currency.title
        holder.itemView.setOnClickListener { listener.onCurrencyClick(currency) }
    }

    interface Listener {
        fun onCurrencyClick(currency: Currency)
    }

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer

    private class DiffCallback : DiffUtil.ItemCallback<Currency>() {

        override fun areItemsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem == newItem
        }
    }
}
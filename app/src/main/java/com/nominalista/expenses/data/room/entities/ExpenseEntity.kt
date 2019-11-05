package com.nominalista.expenses.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.util.extensions.toExactDouble
import com.nominalista.expenses.util.extensions.toExactFloat
import com.nominalista.expenses.util.getCurrentTimestamp
import org.threeten.bp.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "amount") val amount: Float,
    @ColumnInfo(name = "currency") val currency: Currency,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
) {

    fun mapToExpense(tagEntities: List<TagEntity>) =
        Expense(
            id = id.toString(),
            amount = amount.toExactDouble(),
            currency = currency,
            title = title,
            tags = tagEntities.map { it.mapToTag() },
            date = date,
            notes = notes,
            timestamp = createdAt
        )

    companion object {

        fun prepareForInsertion(expense: Expense) =
            ExpenseEntity(
                id = 0,
                amount = expense.amount.toExactFloat(),
                currency = expense.currency,
                title = expense.title,
                date = expense.date,
                notes = expense.notes,
                createdAt = getCurrentTimestamp(),
                modifiedAt = 0L
            )

        fun prepareForUpdate(expense: Expense) =
            ExpenseEntity(
                id = expense.id.toLong(),
                amount = expense.amount.toExactFloat(),
                currency = expense.currency,
                title = expense.title,
                date = expense.date,
                notes = expense.notes,
                createdAt = expense.timestamp ?: 0L,
                modifiedAt = 0L
            )

        fun fromExpense(expense: Expense) =
            ExpenseEntity(
                id = expense.id.toLong(),
                amount = expense.amount.toExactFloat(),
                currency = expense.currency,
                title = expense.title,
                date = expense.date,
                notes = expense.notes,
                createdAt = expense.timestamp ?: 0L,
                modifiedAt = 0L
            )
    }
}
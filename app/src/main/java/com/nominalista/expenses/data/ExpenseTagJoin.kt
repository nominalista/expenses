package com.nominalista.expenses.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index

@Entity(tableName = "expense_tag_joins",
        primaryKeys = ["expense_id", "tag_id"],
        indices = [
            Index(value = ["expense_id"]),
            Index(value = ["tag_id"])
        ],
        foreignKeys = [
            ForeignKey(entity = Expense::class,
                    parentColumns = ["id"],
                    childColumns = ["expense_id"],
                    onDelete = CASCADE),
            ForeignKey(entity = Tag::class,
                    parentColumns = ["id"],
                    childColumns = ["tag_id"],
                    onDelete = CASCADE)
        ])
data class ExpenseTagJoin(
        @ColumnInfo(name = "expense_id") val expenseId: Long,
        @ColumnInfo(name = "tag_id") val tagId: Long
)
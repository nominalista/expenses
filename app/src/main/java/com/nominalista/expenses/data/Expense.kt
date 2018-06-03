package com.nominalista.expenses.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "expenses")
data class Expense(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "amount") val amount: Float,
        @ColumnInfo(name = "currency") val currency: Currency,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "date") val date: Date,
        @ColumnInfo(name = "notes") val notes: String,
        @Ignore var tags: List<Tag>
) : Parcelable {

    // Convenient constructor for Room.
    constructor(
            id: Long,
            amount: Float,
            currency: Currency,
            title: String,
            date: Date,
            notes: String
    ) : this(id, amount, currency, title, date, notes, emptyList())
}
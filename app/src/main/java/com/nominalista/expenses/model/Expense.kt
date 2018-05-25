package com.nominalista.expenses.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "expenses")
data class Expense(
        @PrimaryKey(autoGenerate = true) val id: Int?,
        @ColumnInfo(name = "amount") val amount: Float,
        @ColumnInfo(name = "currency") val currency: Currency,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "user_name") val userName: String,
        @ColumnInfo(name = "date") val date: Date,
        @ColumnInfo(name = "notes") val notes: String
) : Parcelable
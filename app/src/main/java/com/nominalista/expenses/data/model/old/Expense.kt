package com.nominalista.expenses.data.model.old

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.nominalista.expenses.data.model.Currency
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "amount") val amount: Float,
    @ColumnInfo(name = "currency") val currency: Currency,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long,
    @Ignore var tags: List<Tag>
) : Parcelable {

    /**
     * Convenient constructor for Room.
     */
    constructor(
        id: Long,
        amount: Float,
        currency: Currency,
        title: String,
        date: LocalDate,
        notes: String,
        createdAt: Long,
        modifiedAt: Long
    ) : this(id, amount, currency, title, date, notes, createdAt, modifiedAt, emptyList())
}
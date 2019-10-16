package com.nominalista.expenses.data.model.old

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "tags")
data class Tag(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "name") val name: String
) : Parcelable
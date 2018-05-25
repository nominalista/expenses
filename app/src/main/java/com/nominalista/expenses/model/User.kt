package com.nominalista.expenses.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "users")
data class User(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "name") val name: String
) : Parcelable
package com.nominalista.expenses.data.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nominalista.expenses.data.model.Tag
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "name") val name: String
) : Parcelable {

    fun mapToTag() = Tag(id = id.toString(), name = name)

    companion object {

        fun prepareForInsertion(tag: Tag) = TagEntity(id = 0, name = tag.name)

        fun fromTag(tag: Tag) = TagEntity(id = tag.id.toLong(), name = tag.name)
    }
}
package com.nominalista.expenses.ui.home

import android.os.Parcelable
import com.nominalista.expenses.data.Tag
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagFilter(val tags: HashSet<Tag>): Parcelable {

    constructor() : this(HashSet())

    fun add(tag: Tag) = tags.add(tag)

    fun remove(tag: Tag) = tags.remove(tag)

    fun contains(tag: Tag) = tags.contains(tag)

    fun containsAnyOf(tags: List<Tag>) = this.tags.any { tags.contains(it) }
}
package com.nominalista.expenses.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nominalista.expenses.data.Tag

@Dao
interface TagDao {

    @Query("SELECT * FROM tags")
    fun getAll(): List<Tag>

    @Insert
    fun insert(tag: Tag): Long

    @Delete
    fun delete(tag: Tag)
}
package com.nominalista.expenses.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nominalista.expenses.data.room.entities.TagEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface TagDao {

    @Query("SELECT * FROM tags")
    fun observeAll(): Observable<List<TagEntity>>

    @Query("SELECT * FROM tags")
    fun getAll(): Single<List<TagEntity>>

    @Insert
    fun insert(tag: TagEntity): Single<Long>

    @Query("DELETE FROM tags WHERE id = :id")
    fun deleteById(id: Long): Completable

    @Query("DELETE FROM tags")
    fun deleteAll(): Completable
}
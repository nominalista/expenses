package com.nominalista.expenses.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nominalista.expenses.data.room.entities.RuleEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules")
    fun observeAll(): Observable<List<RuleEntity>>

    @Query("SELECT * FROM rules")
    fun getAll(): Single<List<RuleEntity>>

    @Query("SELECT * from rules WHERE id = :id")
    fun observeById(id: Long): Observable<RuleEntity>

    @Query("SELECT * from rules WHERE id = :id")
    fun getById(id: Long): Single<RuleEntity>

    @Update
    fun update(tag: RuleEntity): Completable

    @Insert
    fun insert(tag: RuleEntity): Single<Long>

    @Query("DELETE FROM rules WHERE id = :id")
    fun deleteById(id: Long): Completable

}
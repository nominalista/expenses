package com.nominalista.expenses.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface UserDao {

    @Query("SELECT * from users")
    fun getAll(): Flowable<List<User>>

    @Insert
    fun insert(user: User)

    @Delete
    fun delete(user: User)
}
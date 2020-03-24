package com.nominalista.expenses.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings
import com.nominalista.expenses.data.room.entities.ExpenseTagJoinEntity
import com.nominalista.expenses.data.room.entities.TagEntity
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ExpenseTagJoinDao {

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """
            SELECT * FROM tags 
            INNER JOIN expense_tag_joins
            ON tags.id = expense_tag_joins.tag_id
            WHERE expense_tag_joins.expense_id = :expenseId
        """
    )
    fun observeTagsByExpenseId(expenseId: Long): Observable<List<TagEntity>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        """
            SELECT * FROM tags INNER JOIN expense_tag_joins ON
            tags.id = expense_tag_joins.tag_id WHERE
            expense_tag_joins.expense_id = :expenseId
        """
    )
    fun getTagsByExpenseId(expenseId: Long): Single<List<TagEntity>>

    @Insert
    fun insert(join: ExpenseTagJoinEntity)

    @Query("DELETE FROM expense_tag_joins WHERE expense_tag_joins.expense_id = :expenseId")
    fun deleteByExpenseId(expenseId: Long)
}
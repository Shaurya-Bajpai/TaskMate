package com.example.to_dolist.database

import androidx.room.*
import com.example.to_dolist.data.Todo
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TodoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addTask(conEntity: Todo)

    @Query("Select * from `todo-table`")
    abstract fun getAllTask(): Flow<List<Todo>>

    @Update
    abstract suspend fun updateTask(conEntity: Todo) // Marked as suspend

    @Delete
    abstract suspend fun deleteTask(conEntity: Todo) // Marked as suspend

    @Query("Select * from `todo-table` where id=:id")
    abstract fun getTaskById(id: Long): Flow<Todo>
}
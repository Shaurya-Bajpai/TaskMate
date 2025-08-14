package com.example.taskmate.database

import androidx.room.*
import com.example.taskmate.data.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTask(todo: Todo)

    @Query("SELECT * FROM `todo-table` ORDER BY `todo-priority` DESC, `todo-created-at` DESC")
    fun getAllTask(): Flow<List<Todo>>

    @Query("SELECT * FROM `todo-table` WHERE `todo-is-completed` = 0 ORDER BY `todo-priority` DESC, `todo-due-date` ASC")
    fun getActiveTasks(): Flow<List<Todo>>

    @Query("SELECT * FROM `todo-table` WHERE `todo-is-completed` = 1 ORDER BY `todo-created-at` DESC")
    fun getCompletedTasks(): Flow<List<Todo>>

    @Query("SELECT * FROM `todo-table` WHERE (`todo-title` LIKE :query OR `todo-description` LIKE :query) ORDER BY `todo-priority` DESC")
    fun searchTasks(query: String): Flow<List<Todo>>

    @Query("SELECT * FROM `todo-table` WHERE `todo-category` = :category ORDER BY `todo-priority` DESC")
    fun getTasksByCategory(category: String): Flow<List<Todo>>

    @Update
    suspend fun updateTask(todo: Todo)

    @Delete
    suspend fun deleteTask(todo: Todo)

    @Query("SELECT * FROM `todo-table` WHERE id = :id")
    fun getTaskById(id: Long): Flow<Todo>

    @Query("SELECT COUNT(*) FROM `todo-table` WHERE `todo-is-completed` = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM `todo-table`")
    fun getTotalTaskCount(): Flow<Int>
}
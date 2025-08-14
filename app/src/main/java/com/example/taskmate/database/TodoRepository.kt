package com.example.taskmate.database

import com.example.taskmate.data.Category
import com.example.taskmate.data.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    suspend fun addTask(todo: Todo) = todoDao.addTask(todo)

    suspend fun updateTask(todo: Todo) = todoDao.updateTask(todo)

    suspend fun deleteTask(todo: Todo) = todoDao.deleteTask(todo)

    fun getAllTasks(): Flow<List<Todo>> = todoDao.getAllTask()

    fun getActiveTasks(): Flow<List<Todo>> = todoDao.getActiveTasks()

    fun getCompletedTasks(): Flow<List<Todo>> = todoDao.getCompletedTasks()

    fun searchTasks(query: String): Flow<List<Todo>> = todoDao.searchTasks("%$query%")

    fun getTasksByCategory(category: Category): Flow<List<Todo>> {
        return todoDao.getTasksByCategory(category.name) // Convert Category to String
    }

    fun getTaskById(id: Long): Flow<Todo> = todoDao.getTaskById(id)

    fun getCompletedTaskCount(): Flow<Int> = todoDao.getCompletedTaskCount()

    fun getTotalTaskCount(): Flow<Int> = todoDao.getTotalTaskCount()
}
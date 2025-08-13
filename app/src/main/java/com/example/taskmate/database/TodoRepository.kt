package com.example.taskmate.database

import com.example.taskmate.data.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    suspend fun addTask(todo: Todo) = todoDao.addTask(todo)
    suspend fun updateTask(todo: Todo) = todoDao.updateTask(todo)
    suspend fun deleteTask(todo: Todo) = todoDao.deleteTask(todo)
    fun getTask(): Flow<List<Todo>> = todoDao.getAllTask()
    fun getTaskById(id: Long): Flow<Todo> = todoDao.getTaskById(id)
}
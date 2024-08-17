package com.example.to_dolist.database

import com.example.to_dolist.data.Todo
import kotlinx.coroutines.flow.Flow


class TodoRepository(private val todoDao: TodoDao) {

    suspend fun addTask(todo: Todo){
        todoDao.addTask(todo)
    }

    fun getTask(): Flow<List<Todo>> = todoDao.getAllTask()

    fun getTaskById(id: Long): Flow<Todo> {
        return todoDao.getTaskById(id)
    }

    suspend fun updateTask(todo: Todo){
        todoDao.updateTask(todo)
    }

    suspend fun deleteTask(todo: Todo){
        todoDao.deleteTask(todo)
    }

}
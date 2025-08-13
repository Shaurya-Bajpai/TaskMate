package com.example.to_dolist.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_dolist.data.Todo
import com.example.to_dolist.database.TodoRepository
import com.example.to_dolist.graph.Graph
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class TodoViewModel(
    private var todoRepository: TodoRepository = Graph.todoRepository
): ViewModel() {

    var title by mutableStateOf("")
        private set

    fun titleChange(newString: String) {
        title = newString
    }

    lateinit var getAllTask: Flow<List<Todo>>

    init {
        viewModelScope.launch {
            getAllTask = todoRepository.getTask()
        }
    }

    fun addTask(todo: Todo){
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.addTask(todo = todo)
            title = ""
        }
    }

    fun getTaskById(id: Long): Flow<Todo> {
        return todoRepository.getTaskById(id)
    }

    fun updateTask(todo: Todo){
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTask(todo = todo)
        }
    }

    fun deleteTask(todo: Todo){
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.deleteTask(todo = todo)
        }
    }
}
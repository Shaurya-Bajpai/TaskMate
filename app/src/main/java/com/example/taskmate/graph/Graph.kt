package com.example.taskmate.graph

import android.content.Context
import androidx.room.Room
import com.example.taskmate.database.TodoDatabase
import com.example.taskmate.database.TodoRepository


object Graph {

    lateinit var database: TodoDatabase

    val todoRepository by lazy {
        TodoRepository(todoDao = database.todoDao())
    }

    fun provide(context: Context){
        database = Room.databaseBuilder(context, TodoDatabase::class.java,"todo.db").build()
    }

}
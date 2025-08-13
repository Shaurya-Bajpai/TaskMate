package com.example.taskmate.database

import androidx.room.*
import com.example.taskmate.data.Todo

@Database(entities = [Todo::class], version = 1, exportSchema = false)
abstract class TodoDatabase: RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
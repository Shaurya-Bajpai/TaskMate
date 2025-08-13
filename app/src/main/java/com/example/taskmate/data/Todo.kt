package com.example.taskmate.data

import androidx.room.*

@Entity( tableName = "todo-table")
data class Todo(
    @PrimaryKey( autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo( name = "todo-title")
    val title: String = "",
)
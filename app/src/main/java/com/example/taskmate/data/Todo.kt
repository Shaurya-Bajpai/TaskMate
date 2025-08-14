package com.example.taskmate.data

import androidx.room.*

@Entity(tableName = "todo-table")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "todo-title")
    val title: String = "",
    @ColumnInfo(name = "todo-description")
    val description: String = "",
    @ColumnInfo(name = "todo-priority")
    val priority: Priority = Priority.MEDIUM,
    @ColumnInfo(name = "todo-category")
    val category: Category = Category.PERSONAL,
    @ColumnInfo(name = "todo-due-date")
    val dueDate: Long? = null, // Timestamp
    @ColumnInfo(name = "todo-is-completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "todo-created-at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "todo-color-theme")
    val colorTheme: String = "purple" // For customization
)
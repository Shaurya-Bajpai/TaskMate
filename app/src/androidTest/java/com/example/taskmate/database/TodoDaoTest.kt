package com.example.taskmate.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.taskmate.data.Category
import com.example.taskmate.data.Priority
import com.example.taskmate.data.Todo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TodoDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TodoDatabase
    private lateinit var dao: TodoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TodoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.todoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTodo_returnsTrue() = runBlocking {
        val todo = Todo(
            id = 1,
            title = "Test Task",
            description = "Description",
            priority = Priority.HIGH,
            category = Category.WORK,
            isCompleted = false
        )
        dao.addTask(todo)

        val allTasks = dao.getAllTask().first()
        assertTrue(allTasks.contains(todo))
    }

    @Test
    fun deleteTodo_returnsTrue() = runBlocking {
        val todo = Todo(
            id = 1,
            title = "Test Task",
            description = "Description",
            priority = Priority.HIGH,
            category = Category.WORK,
            isCompleted = false
        )
        dao.addTask(todo)
        dao.deleteTask(todo)

        val allTasks = dao.getAllTask().first()
        assertTrue(allTasks.isEmpty())
    }

    @Test
    fun updateTodo_returnsTrue() = runBlocking {
        val todo = Todo(
            id = 1,
            title = "Test Task",
            description = "Description",
            priority = Priority.HIGH,
            category = Category.WORK,
            isCompleted = false
        )
        dao.addTask(todo)

        val updatedTodo = todo.copy(title = "Updated Task", isCompleted = true)
        dao.updateTask(updatedTodo)

        val allTasks = dao.getAllTask().first()
        assertEquals("Updated Task", allTasks[0].title)
        assertTrue(allTasks[0].isCompleted)
    }
}

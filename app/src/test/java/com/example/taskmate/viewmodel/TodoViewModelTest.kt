package com.example.taskmate.viewmodel

import android.content.Context
import com.example.taskmate.data.Category
import com.example.taskmate.data.Priority
import com.example.taskmate.data.ReminderType
import com.example.taskmate.data.Todo
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.widget.WidgetRefresher
import com.example.taskmate.worker.AlarmScheduler
import com.example.taskmate.worker.ReminderWorker
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TodoRepository
    private lateinit var mockContext: Context
    private lateinit var widgetRefresher: WidgetRefresher
    private lateinit var viewModel: TodoViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        widgetRefresher = mockk(relaxed = true)

        // We mock the ReminderWorker static methods for isolation
        mockkObject(ReminderWorker)
        every { ReminderWorker.scheduleReminder(any(), any()) } just Runs
        every { ReminderWorker.cancelReminder(any(), any()) } just Runs

        mockkObject(AlarmScheduler)
        every { AlarmScheduler.scheduleAlarm(any(), any()) } just Runs
        every { AlarmScheduler.cancelAlarm(any(), any()) } just Runs

        viewModel = TodoViewModel(repository, mockContext, widgetRefresher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun addTask_success_updatesStateAndSchedulesReminder() = runTest {
        // Arrange
        val todo = Todo(
            id = 2L,
            title = "Test task",
            priority = Priority.MEDIUM,
            category = Category.PERSONAL,
            dueDate = 1000L
        )
        coEvery { repository.addTask(any()) } returns 5L

        // Act
        viewModel.addTask(todo)

        // Assert
        coVerify { repository.addTask(todo) }
        verify { ReminderWorker.scheduleReminder(mockContext, todo.copy(id = 5L)) }
        coVerify { widgetRefresher.refresh(mockContext) }

        // Assert UI State successfully resets loading and clears errors
        val finalState = viewModel.uiState.value
        assertEquals(false, finalState.isLoading)
        assertNull(finalState.errorMessage)
    }

    @Test
    fun addTask_error_updatesStateWithError() = runTest {
        // Arrange
        val todo = Todo(title = "Fail task")
        val errorMessage = "Database error"
        coEvery { repository.addTask(any()) } throws Exception(errorMessage)

        // Act
        viewModel.addTask(todo)

        // Assert setup logic does not schedule anything due to exception
        verify(exactly = 0) { ReminderWorker.scheduleReminder(any(), any()) }
        coVerify(exactly = 0) { widgetRefresher.refresh(any()) }

        val finalState = viewModel.uiState.value
        assertEquals(false, finalState.isLoading)
        assertEquals("Failed to add task: $errorMessage", finalState.errorMessage)
    }

    @Test
    fun updateTask_incomplete_reschedulesReminder() = runTest {
        // Arrange
        val todo = Todo(id = 1L, isCompleted = false)
        
        // Act
        viewModel.updateTask(todo)

        // Assert
        coVerify { repository.updateTask(todo) }
        verify { ReminderWorker.scheduleReminder(mockContext, todo) }
        verify(exactly = 0) { ReminderWorker.cancelReminder(any(), any()) }
        coVerify { widgetRefresher.refresh(mockContext) }
    }

    @Test
    fun updateTask_error_doesNotRefreshWidget() = runTest {
        // Arrange
        val todo = Todo(id = 1L, isCompleted = false)
        coEvery { repository.updateTask(any()) } throws Exception("DB error")

        // Act
        viewModel.updateTask(todo)

        // Assert: widget must not be refreshed when the underlying update failed
        coVerify(exactly = 0) { widgetRefresher.refresh(any()) }
        assertEquals("Failed to update task: DB error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateTask_completed_cancelsReminder() = runTest {
        // Arrange
        val todo = Todo(id = 1L, isCompleted = true)
        
        // Act
        viewModel.updateTask(todo)

        // Assert
        coVerify { repository.updateTask(todo) }
        verify { ReminderWorker.cancelReminder(mockContext, 1L) }
        verify { AlarmScheduler.cancelAlarm(mockContext, 1L) }
        verify(exactly = 0) { ReminderWorker.scheduleReminder(any(), any()) }
    }

    @Test
    fun deleteTask_cancelsReminder() = runTest {
        // Arrange
        val todo = Todo(id = 3L)
        
        // Act
        viewModel.deleteTask(todo)

        // Assert
        coVerify { repository.deleteTask(todo) }
        verify { ReminderWorker.cancelReminder(mockContext, 3L) }
        verify { AlarmScheduler.cancelAlarm(mockContext, 3L) }
        coVerify { widgetRefresher.refresh(mockContext) }
    }

    @Test
    fun deleteTask_error_doesNotRefreshWidget() = runTest {
        // Arrange
        val todo = Todo(id = 3L)
        coEvery { repository.deleteTask(any()) } throws Exception("DB error")

        // Act
        viewModel.deleteTask(todo)

        // Assert: widget must not be refreshed when the underlying delete failed
        coVerify(exactly = 0) { widgetRefresher.refresh(any()) }
        assertEquals("Failed to delete task: DB error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun addTask_alarmType_schedulesAlarmNotNotification() = runTest {
        // Arrange
        val todo = Todo(
            id = 4L,
            title = "Alarm task",
            dueDate = 1000L,
            reminderType = ReminderType.ALARM
        )
        coEvery { repository.addTask(any()) } returns 6L

        // Act
        viewModel.addTask(todo)

        // Assert
        verify { AlarmScheduler.scheduleAlarm(mockContext, todo.copy(id = 6L)) }
        verify(exactly = 0) { ReminderWorker.scheduleReminder(any(), any()) }
    }

    @Test
    fun updateTask_noneType_cancelsBothReminders() = runTest {
        // Arrange
        val todo = Todo(id = 5L, isCompleted = false, reminderType = ReminderType.NONE)

        // Act
        viewModel.updateTask(todo)

        // Assert
        verify { ReminderWorker.cancelReminder(mockContext, 5L) }
        verify { AlarmScheduler.cancelAlarm(mockContext, 5L) }
        verify(exactly = 0) { ReminderWorker.scheduleReminder(any(), any()) }
        verify(exactly = 0) { AlarmScheduler.scheduleAlarm(any(), any()) }
    }
}


package com.example.taskmate.widget

import android.content.Context
import com.example.taskmate.data.ReminderType
import com.example.taskmate.data.Todo
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.worker.AlarmScheduler
import com.example.taskmate.worker.ReminderWorker
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Covers the widget checkbox tap: tapping it in the widget invokes ToggleTaskActionCallback,
// which delegates to toggleTaskCompletion(). Tested directly against a mock TodoRepository so
// it doesn't need the real Hilt EntryPoint/Glance stack.
@OptIn(ExperimentalCoroutinesApi::class)
class ToggleTaskActionCallbackTest {

    private lateinit var repository: TodoRepository
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        mockkObject(ReminderWorker)
        every { ReminderWorker.cancelReminder(any(), any()) } just Runs

        mockkObject(AlarmScheduler)
        every { AlarmScheduler.cancelAlarm(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun toggleTaskCompletion_incompleteTask_marksCompleteAndCancelsReminders() = runTest {
        // Arrange: an active task with a notification reminder
        val task = Todo(id = 1L, title = "Buy milk", isCompleted = false, reminderType = ReminderType.NOTIFICATION)
        every { repository.getTaskById(1L) } returns flowOf(task)

        // Act: simulate tapping the widget's checkbox
        val result = toggleTaskCompletion(repository, mockContext, taskId = 1L)

        // Assert
        assertTrue(result)
        coVerify { repository.updateTask(task.copy(isCompleted = true)) }
        verify { ReminderWorker.cancelReminder(mockContext, 1L) }
        verify { AlarmScheduler.cancelAlarm(mockContext, 1L) }
    }

    @Test
    fun toggleTaskCompletion_completedTask_marksIncompleteWithoutCancellingReminders() = runTest {
        // Arrange: a completed task
        val task = Todo(id = 2L, title = "Already done", isCompleted = true)
        every { repository.getTaskById(2L) } returns flowOf(task)

        // Act: tapping the checkbox again should uncheck it
        val result = toggleTaskCompletion(repository, mockContext, taskId = 2L)

        // Assert
        assertFalse(result)
        coVerify { repository.updateTask(task.copy(isCompleted = false)) }
        verify(exactly = 0) { ReminderWorker.cancelReminder(any(), any()) }
        verify(exactly = 0) { AlarmScheduler.cancelAlarm(any(), any()) }
    }

    @Test
    fun toggleTaskCompletion_readsTheSpecificTaskIdRequested() = runTest {
        val task = Todo(id = 42L, title = "Specific task", isCompleted = false)
        every { repository.getTaskById(42L) } returns flowOf(task)

        toggleTaskCompletion(repository, mockContext, taskId = 42L)

        verify { repository.getTaskById(42L) }
    }
}

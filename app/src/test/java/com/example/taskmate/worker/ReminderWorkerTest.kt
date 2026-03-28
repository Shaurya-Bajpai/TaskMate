package com.example.taskmate.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testReminderWorker_doWork_returnsSuccess() = runBlocking {
        // Arrange
        // We supply valid mock data to simulate what it would receive
        val inputData = workDataOf(
            ReminderWorker.KEY_TITLE to "Test Task",
            ReminderWorker.KEY_TASK_ID to 1L
        )

        // Build the worker specifically tailored for tests
        val worker = TestListenableWorkerBuilder<ReminderWorker>(context)
            .setInputData(inputData)
            .build()

        // Act
        // Execute doWork precisely as WorkManager will do
        val result = worker.doWork()

        // Assert
        // Given Robolectric environment, the NotificationCompat executes normally under the hood, 
        // yielding Result.success() upon conclusion.
        assertEquals(Result.success(), result)
    }

    @Test
    fun testReminderWorker_doWork_returnsSuccess_withEmptyData() = runBlocking {
        // Arrange
        // Suppose no data actually propagated just in case
        val worker = TestListenableWorkerBuilder<ReminderWorker>(context).build()

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(Result.success(), result)
    }
}

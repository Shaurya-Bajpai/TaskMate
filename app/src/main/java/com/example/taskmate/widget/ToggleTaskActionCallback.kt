package com.example.taskmate.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.di.widgetEntryPoint
import com.example.taskmate.worker.AlarmScheduler
import com.example.taskmate.worker.ReminderWorker
import kotlinx.coroutines.flow.firstOrNull

// Toggles a task's completion and cancels its reminder/alarm when marked done. Pulled out of
// ToggleTaskActionCallback so it can be unit tested with a mock TodoRepository instead of
// needing the real Hilt EntryPoint/Glance stack.
internal suspend fun toggleTaskCompletion(repository: TodoRepository, context: Context, taskId: Long): Boolean {
    // Handle the case where the task may have been deleted in-app between rendering the widget
    // and the user tapping the widget. Using firstOrNull avoids NoSuchElementException.
    val task = repository.getTaskById(taskId).firstOrNull()
        ?: run {
            Log.w("ToggleTaskAction", "Task $taskId not found when toggling")
            return false
        }

    val updated = task.copy(isCompleted = !task.isCompleted)
    try {
        repository.updateTask(updated)
    } catch (t: Throwable) {
        Log.e("ToggleTaskAction", "Failed to update task $taskId", t)
        return task.isCompleted
    }

    if (updated.isCompleted) {
        ReminderWorker.cancelReminder(context, taskId)
        AlarmScheduler.cancelAlarm(context, taskId)
    }

    return updated.isCompleted
}

class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return
        try {
            val repository = context.widgetEntryPoint().todoRepository()
            try {
                toggleTaskCompletion(repository, context, taskId)
            } catch (t: Throwable) {
                Log.e("ToggleTaskAction", "Error toggling task $taskId", t)
            }
        } catch (t: Throwable) {
            // If the entry point fails or other unexpected error occurs, log it
            Log.e("ToggleTaskAction", "Action failed for task $taskId", t)
        } finally {
            TaskMateWidget().updateAll(context)
        }
    }
}

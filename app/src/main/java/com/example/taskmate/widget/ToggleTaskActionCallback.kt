package com.example.taskmate.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.taskmate.di.widgetEntryPoint
import com.example.taskmate.worker.AlarmScheduler
import com.example.taskmate.worker.ReminderWorker
import kotlinx.coroutines.flow.first

class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return
        val repository = context.widgetEntryPoint().todoRepository()
        val task = repository.getTaskById(taskId).first()
        val updated = task.copy(isCompleted = !task.isCompleted)
        repository.updateTask(updated)

        if (updated.isCompleted) {
            ReminderWorker.cancelReminder(context, taskId)
            AlarmScheduler.cancelAlarm(context, taskId)
        }

        TaskMateWidget().updateAll(context)
    }
}

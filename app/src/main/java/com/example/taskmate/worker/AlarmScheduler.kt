package com.example.taskmate.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.taskmate.MainActivity
import com.example.taskmate.data.ReminderOffset
import com.example.taskmate.data.Todo
import com.example.taskmate.data.reminderRequestId
import com.example.taskmate.data.snoozeRequestId
import com.example.taskmate.receiver.AlarmReceiver
import java.util.concurrent.TimeUnit
import kotlin.collections.ifEmpty

object AlarmScheduler {

    /** Schedules one independently-timed alarm per selected reminder offset. */
    fun scheduleAlarm(context: Context, task: Todo) {
        val dueDate = task.dueDate ?: return
        val offsets = ReminderOffset.decodeSet(task.reminderOffsetsMinutes).ifEmpty { setOf(ReminderOffset.AT_DUE_TIME) }

        offsets.forEach { offset ->
            val triggerTime = dueDate - TimeUnit.MINUTES.toMillis(offset.minutes)
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarmAt(context, reminderRequestId(task.id, offset), task.id, task.title, triggerTime)
            }
        }
    }

    fun snoozeAlarm(context: Context, taskId: Long, taskTitle: String, snoozeMinutes: Long = 5L) {
        val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes)
        scheduleAlarmAt(context, snoozeRequestId(taskId), taskId, taskTitle, triggerTime)
    }

    /** Cancels every possible per-offset alarm (and any pending snooze) for this task. */
    fun cancelAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestIds = ReminderOffset.entries.map { reminderRequestId(taskId, it) } + snoozeRequestId(taskId)
        requestIds.forEach { requestId ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestId,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun scheduleAlarmAt(context: Context, requestId: Int, taskId: Long, taskTitle: String, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, requestId)
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Shown if the user taps the system status-bar "next alarm" indicator.
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestId,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
            operationPendingIntent
        )
    }
}

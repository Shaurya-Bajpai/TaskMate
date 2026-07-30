package com.example.taskmate.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.taskmate.MainActivity
import com.example.taskmate.data.Todo
import com.example.taskmate.receiver.AlarmReceiver
import java.util.concurrent.TimeUnit

object AlarmScheduler {

    fun scheduleAlarm(context: Context, task: Todo) {
        val dueDate = task.dueDate ?: return
        val offsetMinutes = task.reminderOffsetMinutes ?: 0L
        val triggerTime = dueDate - TimeUnit.MINUTES.toMillis(offsetMinutes)

        if (triggerTime <= System.currentTimeMillis()) return // Alarm time has already passed

        scheduleAlarmAt(context, task.id, task.title, triggerTime)
    }

    fun snoozeAlarm(context: Context, taskId: Long, taskTitle: String, snoozeMinutes: Long = 5L) {
        val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes)
        scheduleAlarmAt(context, taskId, taskTitle, triggerTime)
    }

    fun cancelAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleAlarmAt(context: Context, taskId: Long, taskTitle: String, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
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
            taskId.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
            operationPendingIntent
        )
    }
}

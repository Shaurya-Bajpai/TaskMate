package com.example.taskmate.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.taskmate.data.Todo
import com.example.taskmate.R
import com.example.taskmate.data.ReminderOffset
import com.example.taskmate.data.reminderRequestId
import com.example.taskmate.notification.NotificationIcons
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach
import kotlin.collections.ifEmpty

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString(KEY_TITLE) ?: applicationContext.getString(R.string.default_task_reminder)
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, taskId.toInt())
        val offsetMinutes = inputData.getLong(KEY_OFFSET_MINUTES, 0L)

        showNotification(taskTitle, taskId, notificationId, offsetMinutes)
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, taskId: Long, notificationId: Int, offsetMinutes: Long) {
        val channelId = "task_reminder_channel"

        val intent = android.content.Intent(applicationContext, Class.forName("com.example.taskmate.MainActivity")).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        val pendingIntent: android.app.PendingIntent = android.app.PendingIntent.getActivity(
            applicationContext, 
            notificationId, 
            intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.task_reminder_channel_name)
            val descriptionText = applicationContext.getString(R.string.task_reminder_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val contentTitle = if (offsetMinutes > 0) {
            applicationContext.getString(R.string.task_due_soon)
        } else {
            applicationContext.getString(R.string.task_due)
        }
        val contentText = when {
            offsetMinutes <= 0 -> title
            offsetMinutes % 1440 == 0L -> applicationContext.getString(
                R.string.task_due_soon_days_format, offsetMinutes / 1440
            ) + " · " + title
            else -> applicationContext.getString(
                R.string.task_due_soon_format, offsetMinutes
            ) + " · " + title
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(NotificationIcons.appLargeIcon(applicationContext))
            .setColor(getColor(applicationContext, R.color.notification_accent))
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
            }
        } else {
            NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
        }
    }

    companion object {
        const val KEY_TITLE = "task_title"
        const val KEY_TASK_ID = "task_id"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_OFFSET_MINUTES = "offset_minutes"

        /** Schedules one independently-timed notification per selected reminder offset. */
        fun scheduleReminder(context: Context, task: Todo) {
            val dueDate = task.dueDate ?: return
            val offsets = ReminderOffset.decodeSet(task.reminderOffsetsMinutes).ifEmpty { setOf(ReminderOffset.AT_DUE_TIME) }

            offsets.forEach { offset ->
                val triggerTime = dueDate - TimeUnit.MINUTES.toMillis(offset.minutes)
                val delay = triggerTime - System.currentTimeMillis()
                if (delay <= 0) return@forEach // This offset's reminder time has already passed

                val inputData = workDataOf(
                    KEY_TITLE to task.title,
                    KEY_TASK_ID to task.id,
                    KEY_NOTIFICATION_ID to reminderRequestId(task.id, offset),
                    KEY_OFFSET_MINUTES to offset.minutes
                )

                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "reminder_${task.id}_${offset.minutes}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }

        /** Cancels every possible per-offset reminder for this task (safe even if only some were scheduled). */
        fun cancelReminder(context: Context, taskId: Long) {
            ReminderOffset.entries.forEach { offset ->
                WorkManager.getInstance(context).cancelUniqueWork("reminder_${taskId}_${offset.minutes}")
            }
        }
    }
}


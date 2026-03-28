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
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.taskmate.data.Todo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString(KEY_TITLE) ?: "Task Reminder"
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        
        showNotification(taskTitle, taskId.toInt())
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, notificationId: Int) {
        val channelId = "task_reminder_channel"

        val intent = android.content.Intent(applicationContext, Class.forName("com.example.taskmate.MainActivity")).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", notificationId.toLong())
        }
        val pendingIntent: android.app.PendingIntent = android.app.PendingIntent.getActivity(
            applicationContext, 
            notificationId, 
            intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Notifications for due tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle("Task Due")
            .setContentText(title)
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

        fun scheduleReminder(context: Context, task: Todo) {
            val dueDate = task.dueDate ?: return
            val currentTime = System.currentTimeMillis()
            val delay = dueDate - currentTime

            if (delay <= 0) return // Time has passed

            val inputData = workDataOf(
                KEY_TITLE to task.title,
                KEY_TASK_ID to task.id
            )

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        fun cancelReminder(context: Context, taskId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
        }
    }
}

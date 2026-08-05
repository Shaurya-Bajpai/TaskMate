package com.example.taskmate.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.taskmate.R
import com.example.taskmate.alarm.AlarmRingActivity
import com.example.taskmate.alarm.AlarmSound
import com.example.taskmate.notification.NotificationIcons

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        // Nothing here should ever be allowed to bring down the receiver silently — a broken
        // OEM ROM quirk in any one step (channel creation, notification post, activity launch)
        // must not cost the user the steps that would otherwise still have worked.
        try {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE)
                ?: context.getString(R.string.default_task_reminder)
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, taskId.toInt())

            createAlarmChannel(context)

            val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            val ringPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                ringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(NotificationIcons.appLargeIcon(context))
                .setColor(ContextCompat.getColor(context, R.color.notification_accent))
                .setContentTitle(context.getString(R.string.task_alarm_title))
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(ringPendingIntent, true)
                .setContentIntent(ringPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(notificationId, notification)
                }
            } catch (e: Exception) {
                // Posting the notification and launching the ring activity are independent
                // fallbacks for each other (see AlarmRingActivity's own full-screen-intent
                // comment) — one failing must not skip the other.
                Log.e(TAG, "Failed to post alarm notification for task=$taskId", e)
            }

            // Full-screen intent isn't guaranteed to auto-launch on every OEM/state, so also
            // start the ringing activity directly. This is a documented background-start exemption
            // for apps responding to an AlarmManager#setAlarmClock alarm going off.
            try {
                context.startActivity(ringIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch AlarmRingActivity for task=$taskId", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unhandled failure in AlarmReceiver.onReceive", e)
        }
    }
    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val ALARM_CHANNEL_ID = "task_alarm_channel"

        private fun createAlarmChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val alarmSound = AlarmSound.candidateUris(context).firstOrNull()

            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                context.getString(R.string.task_alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.task_alarm_channel_desc)
                setSound(alarmSound, audioAttributes)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

package com.example.taskmate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.taskmate.data.ReminderType
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.worker.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TodoRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeTasks = repository.getActiveTasks().first()
                activeTasks
                    .filter { it.reminderType == ReminderType.ALARM && it.dueDate != null }
                    .forEach { task ->
                        // AlarmScheduler itself no longer throws on a scheduling failure, but this
                        // stays defensive-in-depth: one malformed task must never stop every task
                        // after it in the list from being rescheduled post-reboot.
                        try {
                            AlarmScheduler.scheduleAlarm(context, task)
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Failed to reschedule alarm for task=${task.id}", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

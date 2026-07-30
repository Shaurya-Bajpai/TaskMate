package com.example.taskmate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
                    .forEach { AlarmScheduler.scheduleAlarm(context, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

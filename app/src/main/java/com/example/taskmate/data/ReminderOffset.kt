package com.example.taskmate.data

import androidx.annotation.StringRes
import com.example.taskmate.R

enum class ReminderOffset(val minutes: Long, @StringRes val displayName: Int) {
    AT_DUE_TIME(0L, R.string.reminder_at_due_time),
    FIVE_MINUTES(5L, R.string.reminder_5_min_before),
    FIFTEEN_MINUTES(15L, R.string.reminder_15_min_before),
    THIRTY_MINUTES(30L, R.string.reminder_30_min_before),
    ONE_HOUR(60L, R.string.reminder_1_hour_before),
    ONE_DAY(1440L, R.string.reminder_1_day_before);

    companion object {
        /** Serializes a set of offsets to a comma-separated minutes string for storage, e.g. "0,60,1440". */
        fun encodeSet(offsets: Set<ReminderOffset>): String? =
            if (offsets.isEmpty()) null else offsets.sortedBy { it.minutes }.joinToString(",") { it.minutes.toString() }

        /** Parses the stored CSV back into a set of offsets, ignoring any unrecognized values. */
        fun decodeSet(csv: String?): Set<ReminderOffset> {
            if (csv.isNullOrBlank()) return emptySet()
            return csv.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .mapNotNull { minutes -> entries.find { it.minutes == minutes } }
                .toSet()
        }
    }
}

// Each task gets a 100-wide slot of request/notification ids (offset indices 0-5, snooze at 99).
// Room's autoincrement id is a Long; naively doing `taskId.toInt() * 100` truncates first and can
// silently overflow Int range (or wrap on the multiply) once ids climb high enough — on a device
// with a long history of created/deleted tasks that can quietly collide two different tasks onto
// the same request code, so one task's alarm/notification silently overwrites another's. Reducing
// modulo the safe range *before* multiplying keeps every step inside Int bounds.
private const val SLOTS_PER_TASK = 100
private const val MAX_TASK_SLOT = Int.MAX_VALUE / SLOTS_PER_TASK

private fun taskSlotBase(taskId: Long): Int =
    (taskId % MAX_TASK_SLOT).toInt() * SLOTS_PER_TASK

/**
 * A stable, unique int id per (taskId, offset) pair, used as both the WorkManager/AlarmManager
 * request code and the Android notification id — so a task with multiple reminder offsets gets
 * one independently-scheduled, independently-dismissible notification/alarm per offset instead
 * of them overwriting each other.
 */
fun reminderRequestId(taskId: Long, offset: ReminderOffset): Int =
    taskSlotBase(taskId) + ReminderOffset.entries.indexOf(offset)

/** Reserved id slot (outside the 0-5 offset index range) for a one-off snoozed alarm. */
fun snoozeRequestId(taskId: Long): Int = taskSlotBase(taskId) + 99

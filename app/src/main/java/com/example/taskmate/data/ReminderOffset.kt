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

/**
 * A stable, unique int id per (taskId, offset) pair, used as both the WorkManager/AlarmManager
 * request code and the Android notification id — so a task with multiple reminder offsets gets
 * one independently-scheduled, independently-dismissible notification/alarm per offset instead
 * of them overwriting each other.
 */
fun reminderRequestId(taskId: Long, offset: ReminderOffset): Int =
    (taskId.toInt() * 100) + ReminderOffset.entries.indexOf(offset)

/** Reserved id slot (outside the 0-5 offset index range) for a one-off snoozed alarm. */
fun snoozeRequestId(taskId: Long): Int = (taskId.toInt() * 100) + 99

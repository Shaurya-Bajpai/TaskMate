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
        fun fromMinutes(minutes: Long?): ReminderOffset = entries.find { it.minutes == minutes } ?: AT_DUE_TIME
    }
}

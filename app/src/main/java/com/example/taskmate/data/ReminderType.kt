package com.example.taskmate.data

import androidx.annotation.StringRes
import com.example.taskmate.R

enum class ReminderType(@StringRes val displayName: Int) {
    NONE(R.string.reminder_type_none),
    NOTIFICATION(R.string.reminder_type_notification),
    ALARM(R.string.reminder_type_alarm)
}

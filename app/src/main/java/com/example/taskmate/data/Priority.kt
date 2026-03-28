package com.example.taskmate.data

import com.example.taskmate.R

enum class Priority(val displayName: Int, val value: Int) {
    LOW(R.string.priority_low, 1),
    MEDIUM(R.string.priority_medium, 2),
    HIGH(R.string.priority_high, 3)
}
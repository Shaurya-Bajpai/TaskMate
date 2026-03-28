package com.example.taskmate.data

import androidx.annotation.StringRes
import com.example.taskmate.R

// Now using separate emoji and text resources
enum class Category(@StringRes val displayName: Int, @StringRes val icon: Int) {
    WORK(R.string.category_work, R.string.emoji_work),
    PERSONAL(R.string.category_personal, R.string.emoji_personal),
    SHOPPING(R.string.category_shopping, R.string.emoji_shopping),
    HEALTH(R.string.category_health, R.string.emoji_health),
    EDUCATION(R.string.category_education, R.string.emoji_education),
    ENTERTAINMENT(R.string.category_entertainment, R.string.emoji_entertainment),
    TRAVEL(R.string.category_travel, R.string.emoji_travel),
    OTHER(R.string.category_other, R.string.emoji_other)
}
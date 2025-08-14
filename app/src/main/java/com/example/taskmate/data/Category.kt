package com.example.taskmate.data

enum class Category(val displayName: String, val icon: String) {
    WORK("Work", "💼"),
    PERSONAL("Personal", "👤"),
    SHOPPING("Shopping", "🛒"),
    HEALTH("Health", "🏥"),
    EDUCATION("Education", "📚"),
    ENTERTAINMENT("Entertainment", "🎬"),
    TRAVEL("Travel", "✈️"),
    OTHER("Other", "📌")
}
package com.example.taskmate.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import javax.inject.Inject

// Wraps the real Glance call behind an injectable seam so callers (TodoViewModel) can be
// unit tested with a mock instead of hitting AppWidgetManager, which isn't available in plain
// JVM unit tests.
class WidgetRefresher @Inject constructor() {
    suspend fun refresh(context: Context) {
        TaskMateWidget().updateAll(context)
    }
}

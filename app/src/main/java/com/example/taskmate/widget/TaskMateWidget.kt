package com.example.taskmate.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.taskmate.MainActivity
import com.example.taskmate.data.Priority
import com.example.taskmate.data.Todo
import com.example.taskmate.di.widgetEntryPoint
import kotlinx.coroutines.flow.first

val taskIdKey = ActionParameters.Key<Long>("taskId")

private object WidgetColors {
    val Background = ColorProvider(day = Color(0xFF0F172A), night = Color(0xFF0F172A))
    val TextPrimary = ColorProvider(day = Color(0xFFF8FAFC), night = Color(0xFFF8FAFC))
    val TextSecondary = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8))
    val Accent = ColorProvider(day = Color(0xFF8B5CF6), night = Color(0xFF8B5CF6))
    val HighPriority = ColorProvider(day = Color(0xFFEF4444), night = Color(0xFFEF4444))
    val MediumPriority = ColorProvider(day = Color(0xFFF59E0B), night = Color(0xFFF59E0B))
    val LowPriority = ColorProvider(day = Color(0xFF10B981), night = Color(0xFF10B981))
}

private fun openMainActivityIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

class TaskMateWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.widgetEntryPoint().todoRepository()
        val activeTasks = repository.getActiveTasks().first().take(MAX_VISIBLE_TASKS)
        val totalCount = repository.getTotalTaskCount().first()
        val completedCount = repository.getCompletedTaskCount().first()

        provideContent {
            WidgetContent(activeTasks, completedCount, totalCount)
        }
    }

    companion object {
        private const val MAX_VISIBLE_TASKS = 8
    }
}

@Composable
private fun WidgetContent(tasks: List<Todo>, completedCount: Int, totalCount: Int) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.Background)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TaskMate",
                style = TextStyle(
                    color = WidgetColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "$completedCount/$totalCount done",
                style = TextStyle(color = WidgetColors.TextSecondary, fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(WidgetColors.Accent)
                    .clickable(
                        actionStartActivity(
                            openMainActivityIntent(context).apply {
                                putExtra("openAddTask", true)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = WidgetColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active tasks 🎉",
                    style = TextStyle(color = WidgetColors.TextSecondary, fontSize = 13.sp)
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(tasks, itemId = { it.id }) { task ->
                    TaskRow(task, context)
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Todo, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                actionStartActivity(
                    openMainActivityIntent(context).apply {
                        putExtra("taskId", task.id)
                    }
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBox(
            checked = task.isCompleted,
            onCheckedChange = actionRunCallback<ToggleTaskActionCallback>(
                actionParametersOf(taskIdKey to task.id)
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .background(colorProviderFor(task.priority))
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = task.title,
            maxLines = 1,
            style = TextStyle(color = WidgetColors.TextPrimary, fontSize = 14.sp)
        )
    }
}

private fun colorProviderFor(priority: Priority) = when (priority) {
    Priority.HIGH -> WidgetColors.HighPriority
    Priority.MEDIUM -> WidgetColors.MediumPriority
    Priority.LOW -> WidgetColors.LowPriority
}

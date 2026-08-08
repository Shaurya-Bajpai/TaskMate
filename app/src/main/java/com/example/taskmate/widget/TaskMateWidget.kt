package com.example.taskmate.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.example.taskmate.MainActivity
import com.example.taskmate.R
import com.example.taskmate.data.Priority
import com.example.taskmate.data.Todo
import com.example.taskmate.di.widgetEntryPoint
import kotlinx.coroutines.flow.first

val taskIdKey = ActionParameters.Key<Long>("taskId")

private object WidgetColors {
    val Card = ColorProvider(day = Color(0xFF1E293B), night = Color(0xFF1E293B))
    val Background = ColorProvider(day = Color(0xFF0F172A), night = Color(0xFF0F172A))
    val TextPrimary = ColorProvider(day = Color(0xFFF8FAFC), night = Color(0xFFF8FAFC))
    val TextSecondary = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8))
    val HeaderSubtext = ColorProvider(day = Color(0xFF6D7A88), night = Color(0xFF94A3B8))
    val HeaderButtonBg = ColorProvider(day = Color(0xFF6C7A85), night = Color(0xFF94A3B8))
    val Accent = ColorProvider(day = Color(0xFF8B5CF6), night = Color(0xFF8B5CF6))
    val HighPriority = ColorProvider(day = Color(0xFFEF4444), night = Color(0xFFEF4444))
    val MediumPriority = ColorProvider(day = Color(0xFFF59E0B), night = Color(0xFFF59E0B))
    val LowPriority = ColorProvider(day = Color(0xFF10B981), night = Color(0xFF10B981))
}

private fun openMainActivityIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

// Glance's cornerRadius() is a no-op below API 31, but still logs a warning on every
// composition pass. Skipping the call entirely avoids that overhead on older devices.
private fun GlanceModifier.roundedCorners(radius: Dp): GlanceModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this.cornerRadius(radius) else this

class TaskMateWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.widgetEntryPoint().todoRepository()
        val tasks = repository.getAllTasks().first().take(MAX_VISIBLE_TASKS)
        val totalCount = repository.getTotalTaskCount().first()
        val completedCount = repository.getCompletedTaskCount().first()

        provideContent {
            WidgetContent(tasks, completedCount, totalCount)
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
            .cornerRadius(16.dp)
            .appWidgetBackground()
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.widget_header_gradient))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "TaskMate",
                    style = TextStyle(
                        color = WidgetColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$completedCount/$totalCount done",
                    style = TextStyle(color = WidgetColors.TextSecondary, fontSize = 12.sp)
                )
            }
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .roundedCorners(17.dp)
                    .background(WidgetColors.HeaderButtonBg)
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
                    text = "No tasks yet 🎉",
                    style = TextStyle(color = WidgetColors.TextSecondary, fontSize = 13.sp)
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(tasks, itemId = { it.id }) { task ->
                    TaskRow(task, context)
                    Spacer(modifier = GlanceModifier.height(6.dp))
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
            .roundedCorners(10.dp)
            .background(WidgetColors.Card)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBox(
            checked = task.isCompleted,
            onCheckedChange = actionRunCallback<ToggleTaskActionCallback>(
                actionParametersOf(taskIdKey to task.id)
            ),
            colors = CheckboxDefaults.checkBoxColors(
                checkedColor = WidgetColors.Accent,
                uncheckedColor = WidgetColors.TextSecondary
            ),
            modifier = GlanceModifier
                .roundedCorners(8.dp)
                .background(WidgetColors.Card)
                .padding(4.dp)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .roundedCorners(8.dp)
                .background(WidgetColors.Card)
                .clickable(
                    actionStartActivity(
                        openMainActivityIntent(context).apply {
                            putExtra("taskId", task.id)
                        }
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .roundedCorners(4.dp)
                    .background(colorProviderFor(task.priority))
            ) {}
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = task.title,
                maxLines = 1,
                style = TextStyle(
                    color = if (task.isCompleted) WidgetColors.TextSecondary else WidgetColors.TextPrimary,
                    fontSize = 14.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        }
    }
}

private fun colorProviderFor(priority: Priority) = when (priority) {
    Priority.HIGH -> WidgetColors.HighPriority
    Priority.MEDIUM -> WidgetColors.MediumPriority
    Priority.LOW -> WidgetColors.LowPriority
}

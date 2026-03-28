package com.example.taskmate.home.second

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.taskmate.R
import com.example.taskmate.color.TaskMateColors
import com.example.taskmate.data.Category
import com.example.taskmate.data.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FilterType(val displayName: Int, val icon: ImageVector) {
    ALL(R.string.filter_all, Icons.Default.List),
    ACTIVE(R.string.filter_active, Icons.Default.PlayArrow),
    COMPLETED(R.string.filter_done, Icons.Default.Check),
    HIGH_PRIORITY(R.string.filter_high, Icons.Default.KeyboardArrowUp),
    MEDIUM_PRIORITY(R.string.filter_medium, Icons.Default.Close),
    LOW_PRIORITY(R.string.filter_low, Icons.Default.KeyboardArrowDown)
}

fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.WORK -> TaskMateColors.WorkColor
        Category.PERSONAL -> TaskMateColors.PersonalColor
        Category.SHOPPING -> TaskMateColors.ShoppingColor
        Category.HEALTH -> TaskMateColors.HealthColor
        Category.EDUCATION -> TaskMateColors.EducationColor
        Category.ENTERTAINMENT -> TaskMateColors.EntertainmentColor
        Category.TRAVEL -> TaskMateColors.TravelColor
        Category.OTHER -> TaskMateColors.OtherColor
    }
}

fun formatDate(context: Context, timestamp: Long): String {
    val formatter = SimpleDateFormat(context.getString(R.string.date_format_pattern), Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> Color(0xFFEF4444)
        Priority.MEDIUM -> Color(0xFFF59E0B)
        Priority.LOW -> Color(0xFF10B981)
    }
}
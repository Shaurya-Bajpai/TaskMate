package com.example.taskmate.home.second.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmate.data.Priority
import com.example.taskmate.home.second.getPriorityColor

@Composable
fun PriorityChip(priority: Priority, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) getPriorityColor(priority) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = getPriorityColor(priority)
        )
    ) {
        Text(
            text = priority.displayName,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else getPriorityColor(priority),
            textAlign = TextAlign.Center
        )
    }
}

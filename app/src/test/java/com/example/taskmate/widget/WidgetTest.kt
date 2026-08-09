package com.example.taskmate.widget

import androidx.compose.ui.unit.dp
import com.example.taskmate.data.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

// Covers the size-tier boundary that decides which of the two widget layouts (2x3 vs 4x3+)
// gets shown, and the priority-to-color mapping used for each task row's priority dot.
class TaskMateWidgetTest {

    @Test
    fun pickWidgetSizeTier_atOrBelowMediumWidth_returnsMedium() {
        assertEquals(WidgetSizeTier.MEDIUM, pickWidgetSizeTier(WIDGET_SIZE_MEDIUM.width))
        assertEquals(WidgetSizeTier.MEDIUM, pickWidgetSizeTier(1.dp))
        assertEquals(WidgetSizeTier.MEDIUM, pickWidgetSizeTier(WIDGET_SIZE_MEDIUM.width - 1.dp))
    }

    @Test
    fun pickWidgetSizeTier_aboveMediumWidth_returnsLarge() {
        assertEquals(WidgetSizeTier.LARGE, pickWidgetSizeTier(WIDGET_SIZE_MEDIUM.width + 1.dp))
        assertEquals(WidgetSizeTier.LARGE, pickWidgetSizeTier(WIDGET_SIZE_LARGE.width))
        assertEquals(WidgetSizeTier.LARGE, pickWidgetSizeTier(1000.dp))
    }

    @Test
    fun priorityToColor_isDistinctPerPriority() {
        val high = Priority.HIGH.toColor()
        val medium = Priority.MEDIUM.toColor()
        val low = Priority.LOW.toColor()

        assertNotEquals(high, medium)
        assertNotEquals(medium, low)
        assertNotEquals(high, low)
    }

    @Test
    fun priorityToColor_isStablePerPriority() {
        // Same priority should always resolve to the same color, not a new instance each time.
        assertEquals(Priority.HIGH.toColor(), Priority.HIGH.toColor())
    }
}

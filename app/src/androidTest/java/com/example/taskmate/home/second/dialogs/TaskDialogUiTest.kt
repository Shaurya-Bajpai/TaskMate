package com.example.taskmate.home.second.dialogs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmate.data.Todo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDialogUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDateSelectionTransitionsToTimePicker() {
        var dismissed = false
        var confirmed = false

        composeTestRule.setContent {
            TaskDialog(
                todo = null,
                onDismiss = { dismissed = true },
                onConfirm = { /* Do nothing */ }
            )
        }

        // 1. Click on Set Date button to open picker
        composeTestRule.onNodeWithText("Set Date").performClick()
        
        // Picker should be visibly present
        composeTestRule.onAllNodesWithText("Cancel").onFirst().assertIsDisplayed()
        
        // Click Next to proceed since auto-transition was removed for better UX
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // 3. System should transition completely to TimePicker ("Set Time")
        composeTestRule.onNodeWithText("Set Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        
        // 4. Time Picker shouldn't contain a Cancel button on the dialog level directly next to Back/OK
        // We verify navigating back functions properly.
        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.onNodeWithText("Set Time").assertDoesNotExist()

        // We verify the DatePicker dismiss functionality still works.
        composeTestRule.onAllNodesWithText("Cancel").onFirst().performClick()
    }
}

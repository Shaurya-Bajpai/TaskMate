package com.example.taskmate.home.second.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmate.R
import com.example.taskmate.alarm.BatteryOptimizationHelper

/**
 * Nudges the user toward whichever manufacturer-specific "autostart"/background-activity screen
 * exists on this build (or the generic app-info screen as a fallback — see
 * [BatteryOptimizationHelper.openAutoStartSettings]). Shared between the one-time home-screen
 * launch flow and the reminder-type picker, which re-shows it every time "Alarm" is chosen while
 * the app still isn't exempt from battery optimization.
 */
@Composable
fun AutoStartReliabilityDialog(
    hasKnownAutoStartScreen: Boolean,
    onDismissRequest: () -> Unit,
    onNotNow: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.35f),
                                Color(0xFF6366F1).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                stringResource(id = R.string.autostart_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            if (hasKnownAutoStartScreen) {
                Text(
                    stringResource(id = R.string.autostart_desc),
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            } else {
                // No dedicated OEM screen exists on this build, so the button below lands on the
                // generic app-info page instead — spelling out exactly which two rows to tap
                // there as a numbered list (rather than one dense sentence) so it's easy to
                // recall once the user is looking at that unfamiliar system screen.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.autostart_desc_fallback_intro),
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(id = R.string.autostart_desc_fallback_step1),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(id = R.string.autostart_desc_fallback_step2),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onOpenSettings()
                val foundOemScreen = BatteryOptimizationHelper.openAutoStartSettings(context)
                if (!foundOemScreen) {
                    val toastText = context.getString(R.string.autostart_fallback_toast)
                    // Delayed so the reminder lands once the app-info screen the intent above
                    // just opened is actually visible, instead of firing during the activity
                    // transition and being gone before the user has anything to read it against.
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        android.widget.Toast.makeText(context, toastText, android.widget.Toast.LENGTH_LONG).show()
                    }, 600)
                }
            }) {
                Text(
                    stringResource(id = R.string.autostart_open_settings),
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onNotNow) {
                Text(
                    stringResource(id = R.string.battery_optimization_not_now),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        },
        containerColor = Color(0xFF2D3748),
        shape = RoundedCornerShape(24.dp)
    )
}

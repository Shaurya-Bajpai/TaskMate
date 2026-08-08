package com.example.taskmate

import android.app.Application
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TodoApp: Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        pushAddTaskShortcut()

        // Compose's AndroidComposeView schedules a delayed Runnable to replay a hover-exit
        // MotionEvent, then asserts (via check()) that the event it captured is still the one
        // waiting when that Runnable finally runs. Mouse-driven input — a physical mouse/trackpad,
        // or moving the cursor over a mirrored device screen in Android Studio's Running Devices
        // tool — sends real hover events, and a window resize (e.g. the keyboard opening) landing
        // in that same narrow window can replace the captured event before the Runnable fires,
        // failing that internal assertion. It's bookkeeping-only: nothing about app state or data
        // is affected, so this specific, exactly-matched crash is swallowed instead of taking the
        // whole app down; anything else still crashes normally.
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val isHoverExitAssertion = throwable is IllegalStateException &&
                    throwable.message == "The ACTION_HOVER_EXIT event was not cleared."
            if (isHoverExitAssertion) {
                android.util.Log.w("TodoApp", "Ignored known Compose hover-exit assertion", throwable)
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun pushAddTaskShortcut() {
        val addTaskLabel = getString(R.string.add_task)
        val shortcut = ShortcutInfoCompat.Builder(this, "add_task")
            .setShortLabel(addTaskLabel)
            .setLongLabel(addTaskLabel)
            .setIcon(IconCompat.createWithResource(this, R.drawable.add_task))
            .setIntent(
                Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("openAddTask", true)
                }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }
}

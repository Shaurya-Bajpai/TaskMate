package com.example.taskmate.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.taskmate.R

object NotificationIcons {
    fun appLargeIcon(context: Context): Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
}

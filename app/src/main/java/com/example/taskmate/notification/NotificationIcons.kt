package com.example.taskmate.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.example.taskmate.R

object NotificationIcons {
    // BitmapFactory.decodeResource can't decode ic_launcher on API 26+: it resolves to the
    // mipmap-anydpi-v26 adaptive-icon XML, not a raster image, so decodeResource returns null
    // and crashes every notification/alarm with an NPE. Drawing the resolved Drawable (which
    // does understand AdaptiveIconDrawable) onto a Bitmap works for both adaptive and raster icons.
    fun appLargeIcon(context: Context): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        val width = drawable?.intrinsicWidth?.takeIf { it > 0 } ?: 1
        val height = drawable?.intrinsicHeight?.takeIf { it > 0 } ?: 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawable?.let {
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, canvas.width, canvas.height)
            it.draw(canvas)
        }
        return bitmap
    }
}

package com.example.taskmate.alarm

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

/**
 * A single source of truth for "which sound should the alarm use," shared between the
 * notification channel (AlarmReceiver) and the looping ringtone player (AlarmRingActivity) so the
 * two can't quietly drift out of sync. Ordered by preference: the actual resolved default alarm
 * sound first, then the raw (unresolved) alarm settings URI, then ringtone, then notification —
 * each is a distinct code path through the content resolver, so a device where one fails (e.g.
 * the actual-default-ringtone lookup returning a URI the media framework can't open — seen on a
 * real Vivo device) may still succeed on another.
 */
object AlarmSound {
    fun candidateUris(context: Context): List<Uri> = listOfNotNull(
        RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM),
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    ).distinct()
}

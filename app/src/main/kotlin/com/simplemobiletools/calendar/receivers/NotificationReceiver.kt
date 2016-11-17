package com.simplemobiletools.calendar.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import com.simplemobiletools.calendar.*
import com.simplemobiletools.calendar.activities.EventActivity
import com.simplemobiletools.calendar.models.Event

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        var EVENT_ID = "event_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = intent.getIntExtra(EVENT_ID, -1)
        if (id == -1)
            return

        val event = DBHelper(context).getEvent(id)
        if (event == null || event.reminderMinutes == -1)
            return

        val pendingIntent = getPendingIntent(context, event)
        val startTime = Formatter.getTime(event.startTS)
        val endTime = Formatter.getTime(event.endTS)
        val title = event.title
        val notification = getNotification(context, pendingIntent, getEventTime(startTime, endTime) + " " + title)
        notificationManager.notify(id, notification)

        if (event.repeatInterval != 0)
            Utils.scheduleNextEvent(context, event)
    }

    private fun getEventTime(startTime: String, endTime: String): String {
        return if (startTime == endTime) startTime else startTime + " - " + endTime
    }

    private fun getPendingIntent(context: Context, event: Event): PendingIntent {
        val intent = Intent(context, EventActivity::class.java)
        intent.putExtra(Constants.EVENT_ID, event)
        return PendingIntent.getActivity(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNotification(context: Context, pendingIntent: PendingIntent, content: String): Notification {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return Notification.Builder(context)
                .setContentTitle(context.resources.getString(R.string.app_name))
                .setContentText(content)
                .setSmallIcon(R.mipmap.calendar)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .build()
    }
}
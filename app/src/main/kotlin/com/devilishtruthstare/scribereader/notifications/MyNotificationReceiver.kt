package com.devilishtruthstare.scribereader.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devilishtruthstare.scribereader.R

class MyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, "my_channel_id")
            .setContentTitle("Timer done!")
            .setContentText("Your scheduled notification fired.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1001, notification)
        }
    }
}
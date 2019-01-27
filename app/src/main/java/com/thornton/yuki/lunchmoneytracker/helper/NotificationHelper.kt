package com.thornton.yuki.lunchmoneytracker.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "lunchmoneytracker_channelid"
private const val CHANNEL_NAME = "lunchmoneytracker_channelname"
private const val CHANNEL_DESC = "lunchmoneytracker_channeldesc"
private val CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
private val NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_DEFAULT
private val NOTIFICATION_ID = AtomicInteger(0)

class NotificationHelper {

    companion object {

        fun registerChannel(context: Context) {
            val channel = createChannel()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        private fun createChannel(): NotificationChannel {
            return NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE).apply {
                description = CHANNEL_DESC
            }
        }

        fun createPendingIntent(context: Context, clazz: Class<out AppCompatActivity>): PendingIntent {
            val intent = Intent(context, clazz).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return PendingIntent.getActivity(context, 0, intent, 0)
        }

        fun sendNotification(context: Context, iconId: Int, title: String, message: String, contentIntent: PendingIntent) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NOTIFICATION_PRIORITY)
                .setContentIntent(contentIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(getNotificationId(), builder.build())
            }
        }

        private fun getNotificationId(): Int {
            return NOTIFICATION_ID.incrementAndGet()
        }
    }
}
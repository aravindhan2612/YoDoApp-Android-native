package com.example.ytsample.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R

open class YTNotification(private val ctx: Context) {

    companion object {
        var CHANNEL_ID: String = "YTSample"
        var notificationManager: NotificationManager? = null
        var builder: NotificationCompat.Builder? = null

    }

    fun getNotificationManager(): NotificationManager? {
        if (notificationManager == null)
            notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.applicationContext.getString(R.string.channel_name)
            val descriptionText = ctx.applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system

            getNotificationManager()?.createNotificationChannel(channel)
        }
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(ctx.applicationContext, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.putExtra("data", "fromoutside")
        }
        return PendingIntent.getActivity(ctx.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getNotificationBuilder(): NotificationCompat.Builder? {
        if (builder == null)
            builder = NotificationCompat.Builder(
                ctx.applicationContext,
                CHANNEL_ID)
        return builder
    }
}
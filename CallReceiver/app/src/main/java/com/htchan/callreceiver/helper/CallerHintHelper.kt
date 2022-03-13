package com.htchan.callreceiver.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.htchan.callreceiver.R

import java.net.URL

class CallerHintHelper {
    companion object {
        val CHANNEL_ID = "Call_Receiver"

        private fun createNotificationChannel(context: Context) {
            val name = "Call Receiver"
            val descriptionText = "caller hint for incoming call"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        private fun pushNotification(context: Context, phoneNumber: String, callerHint: String) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Incoming Call $callerHint")
                .setContentText("Phone Number: $phoneNumber")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(0, builder.build())
            }
        }
        private fun showToast(context: Context, phoneNumber: String, callerHint: String) {
            Looper.prepare()

            Toast.makeText(
                context,
                "Phone Number: $phoneNumber\nMapped Result: $callerHint",
                Toast.LENGTH_LONG
            ).show()

            Looper.loop()
        }

        fun show(context: Context, phoneNumber: String, callerHint: String) {
            val sharedPref = context.getSharedPreferences("com.htchan.callreceiver", Context.MODE_PRIVATE)
            val useNotification = sharedPref.getBoolean("use_notification", true)
            val useToast = sharedPref.getBoolean("use_toast", true)
            if (useNotification) {
                createNotificationChannel(context)
                pushNotification(context, phoneNumber, callerHint)
            }
            if (useToast) {
                showToast(context, phoneNumber, callerHint)
            }
        }

        fun map(phoneNumber: String): String {
            val url = URL("https://www.hkjunkcall.com/?ft=$phoneNumber")
            var response = url.readText()
            val regex = "<meta property=\"og:title\" content=\".*?: (.*?) 電話 搜尋結果\"".toRegex()
            var result = regex.find(response)?.groupValues?.get(1)?:  "Unknown"
            return result
        }
    }
}
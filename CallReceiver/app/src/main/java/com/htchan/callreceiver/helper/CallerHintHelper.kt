package com.htchan.callreceiver.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.htchan.callreceiver.MainActivity
import com.htchan.callreceiver.R
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CallerHintHelper {
    val CHANNEL_ID = "Call_Receiver"
    val MAX_ATTEMPT = 2
    val UNKNOWN_CALLER_NAME = "Unknown Caller"
    val UNKNOWN_CALLER_KEYWORD = "找不到其他提交資料"
    val FAIL_CALLER_NAME = "Fail to Fetch Caller Data"

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
        Log.e("call receiver", callerHint)
        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("Phone Number: $phoneNumber")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        if (callerHint == UNKNOWN_CALLER_NAME) {
            val intent = MainActivity.newIntent(context, phoneNumber)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder = builder.setContentTitle("Open App")
                .setContentIntent(pendingIntent)
        } else {
            builder = builder.setContentTitle("Incoming Call $callerHint")
        }
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
        val sharedPref =
            context.getSharedPreferences("com.htchan.callreceiver", Context.MODE_PRIVATE)
        val useNotification = sharedPref.getBoolean("use_notification", true)
        val useToast = sharedPref.getBoolean("use_toast", true)
        if (useNotification) {
            createNotificationChannel(context)
            pushNotification(context, phoneNumber, callerHint)
        }
        if (useToast && !PowerHelper(context).isPowerSaving()) {
            showToast(context, phoneNumber, callerHint)
        }
    }

    fun map(phoneNumber: String): String {
        var name: String = ""
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(
                    "https://hkjunkcall.com/".toHttpUrl()
                        ?.newBuilder()
                        ?.addQueryParameter("ft", phoneNumber)
                        ?.build()
                )
                .build()

            val response = client.newCall(request).execute()
            var content = response.body?.string()
            response.body?.close()
            content = content ?: ""

            if (content.isBlank() == true) {
                return FAIL_CALLER_NAME
            } else if (content.contains(UNKNOWN_CALLER_KEYWORD) == true) {
                return UNKNOWN_CALLER_NAME
            }

            // return findTagArray(content).joinToString(separator = "\n")
            return findStatHtml(content)
        } catch (e: Exception) {
            Log.d("caller logging", e.message ?: "no message")
            name = FAIL_CALLER_NAME
        }
        return name
    }

    fun findTagArray(content: String): List<String> {
        var processing = content.replace("[\r\n\t ]".toRegex(), "")
        processing =
            "<divclass=\"hr-text\"data-content=\"已提交資料\"id=\"已提交資料\"></div>(.*?)<buttontype=\"button\"class=\"btnbtn-infowhite_link\">".toRegex()
                .find(processing)?.groupValues?.get(0) ?: return listOf(FAIL_CALLER_NAME)

        val result = "<!--googleoff:on-->([^<]*?)</font></td>".toRegex().findAll(processing)
            .map { it.groupValues.get(1) ?: "" }.filter { it.isNotBlank() }.toMutableList()
        return result
    }

    fun findStatHtml(content: String): String {
        var result = content.replace("[\r\n\t]".toRegex(), "")
        result =
            "個別回報資料不會構成任何攔截動作.*?((<table.*?</table> *)+)".toRegex()
                .find(result)?.groupValues?.get(1)?: FAIL_CALLER_NAME
        result = result.replace("<table[a-z0-9 %=]*? class=ads.*?</table>".toRegex(), "")
        return result 
    }
}
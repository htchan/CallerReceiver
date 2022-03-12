package com.htchan.callreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class CallReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!PermissionUtils(context).validatePermission()) return

        val state: String? = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            val phoneNumber = intent?.getStringExtra(
                TelephonyManager.EXTRA_INCOMING_NUMBER
            )?: return
            Log.e("CallReceiver", phoneNumber)
            CoroutineScope(Dispatchers.IO).launch {
                HandleIncomingCall(context, phoneNumber)
            }
        }
    }

    fun contactExist(context: Context?, phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor: Cursor = context?.contentResolver?.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        ) ?: return false
        if (cursor.moveToFirst()) return true
        cursor.close()
        return false
    }

    fun mapPhoneNumber(phoneNumber: String): String {
        val url = URL("https://www.hkjunkcall.com/?ft=$phoneNumber")
        var response = url.readText()
        val regex = "<meta property=\"og:title\" content=\".*?: (.*?) 電話 搜尋結果\"".toRegex()
        var result = regex.find(response)?.groupValues?.get(1)?:  "Unknown"
        return result
    }

    suspend fun HandleIncomingCall(context: Context?, phoneNumber: String) {
        withContext(Dispatchers.IO) {
            if (!contactExist(context, phoneNumber)) {
                val contactName: String = mapPhoneNumber(phoneNumber)
                Looper.prepare()
                Toast.makeText(
                    context,
                    "Phone Number: $phoneNumber\nMapped Result: $contactName",
                    Toast.LENGTH_LONG
                ).show()
                Looper.loop()
            }
        }
    }
}
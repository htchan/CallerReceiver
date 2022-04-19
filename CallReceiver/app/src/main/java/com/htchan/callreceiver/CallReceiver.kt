package com.htchan.callreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.htchan.callreceiver.helper.PermissionHelper
import com.htchan.callreceiver.helper.ContactHelper
import com.htchan.callreceiver.helper.CallerHintHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!PermissionHelper(context).validatePermission()) return

        val state: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            val phoneNumber = intent.getStringExtra(
                TelephonyManager.EXTRA_INCOMING_NUMBER
            )?.replace("[\\+| ]".toRegex(), "") ?: return
            CoroutineScope(Dispatchers.IO).launch {
                HandleIncomingCall(context, phoneNumber)
            }
        }
    }

    fun HandleIncomingCall(context: Context, phoneNumber: String) {
        if (!ContactHelper.phoneNumberExist(context, phoneNumber)) {
            val callerHintHelper = CallerHintHelper()
            val callerHint: String = callerHintHelper.map(phoneNumber)
            callerHintHelper.show(context, phoneNumber, callerHint)
        }
    }
}
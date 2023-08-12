package com.htchan.callreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.htchan.callreceiver.helper.CallerHintHelper
import com.htchan.callreceiver.helper.ContactHelper
import com.htchan.callreceiver.helper.PermissionHelper
import com.htchan.callreceiver.helper.PowerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {
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
            var callerHint: String = callerHintHelper.UNKNOWN_CALLER_NAME
            if (!PowerHelper(context).isPowerSaving()) callerHintHelper.map(context, phoneNumber)
            callerHintHelper.show(context, phoneNumber, callerHint)
        }
    }
}
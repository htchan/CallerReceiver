package com.htchan.callreceiver.helper

import android.provider.ContactsContract
import android.net.Uri
import android.database.Cursor
import android.content.Context

class ContactHelper {
    companion object {
        fun phoneNumberExist(context: Context, phoneNumber: String): Boolean {
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
    }
}
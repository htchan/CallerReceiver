package com.htchan.callreceiver.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper(private val context: Context) {
    companion object {
        val ALL_PERMISSIONS = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET
        )
    }

    fun permissionGrant(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun validatePermission(): Boolean {
        for (permission in ALL_PERMISSIONS) {
            if (!permissionGrant(permission)) return false
        }
        return true
    }

    private fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            ALL_PERMISSIONS,
            1
        )
    }

    fun handlePermission(activity: Activity) {
        if (!validatePermission()) {
            requestPermission(activity)
        }
    }
}
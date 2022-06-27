package com.htchan.callreceiver.helper

import android.content.Context
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity

class PowerHelper(private val context: Context) {
    fun isPowerSaving(): Boolean {
        val powerManager = context.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }
}
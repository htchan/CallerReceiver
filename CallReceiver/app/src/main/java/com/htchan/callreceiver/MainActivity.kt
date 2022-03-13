package com.htchan.callreceiver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.pm.PackageManager

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.widget.Switch
import com.htchan.callreceiver.helper.CallerHintHelper
import com.htchan.callreceiver.helper.PermissionHelper


class MainActivity : AppCompatActivity() {
    private val permissionUtils = PermissionHelper(this)
    private lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionUtils.handlePermission(this)
        initSharedPref()
        val permissionText = findViewById<TextView>(R.id.permission_text)
        val receiverText = findViewById<TextView>(R.id.receiver_text)
        if (!permissionUtils.validatePermission()) {
            permissionText.text = getString(R.string.permission_not_grant)
            permissionText.setTextColor(getColor(R.color.fail))
            receiverText.text = getString(R.string.receiver_blocked)
            receiverText.setTextColor(getColor(R.color.fail))
        } else {
            renderEnableButton()
        }
        renderSwitches()
    }

    override fun onResume() {
        super.onResume()
        permissionUtils.handlePermission(this)
        renderEnableButton()
        renderSwitches()
    }

    private fun renderEnableButton() {
        val receiverName = ComponentName(this, "com.htchan.callreceiver.CallReceiver")
        val receiverEnabled = this.packageManager.getComponentEnabledSetting(receiverName)
        val permissionText = findViewById<TextView>(R.id.permission_text)
        val receiverText = findViewById<TextView>(R.id.receiver_text)
        val button = findViewById<Button>(R.id.enable_button)
        if (receiverEnabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            permissionText.text = getString(R.string.permission_grant)
            permissionText.setTextColor(getColor(R.color.pass))
            receiverText.text = getString(R.string.receiver_enabled)
            receiverText.setTextColor(getColor(R.color.pass))
            button.text = getString(R.string.disable)
            button.setOnClickListener {
                this.packageManager.setComponentEnabledSetting(
                    receiverName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                renderEnableButton()
            }
        } else {
            permissionText.text = getString(R.string.permission_grant)
            permissionText.setTextColor(getColor(R.color.pass))
            receiverText.text = getString(R.string.receiver_disabled)
            receiverText.setTextColor(getColor(R.color.fail))
            button.text = getString(R.string.enable)
            button.setOnClickListener {
                this.packageManager.setComponentEnabledSetting(
                    receiverName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                renderEnableButton()
            }
        }
    }

    private fun initSharedPref() {
        sharedPref = getSharedPreferences("com.htchan.callreceiver", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            if (!sharedPref.contains("use_toast")) {
                putBoolean("use_toast", true)
            }
            if (!sharedPref.contains("use_notification")) {
                putBoolean("use_notification", true)
            }
            apply()
        }
    }

    private fun renderSwitches() {
        val validPermission = permissionUtils.validatePermission()
        val toastSwitch = findViewById<Switch>(R.id.toast_switch)
        if (!validPermission) toastSwitch.isEnabled = false
        toastSwitch.isChecked = sharedPref.getBoolean("use_toast", true)
        toastSwitch.setOnCheckedChangeListener { _, b: Boolean ->
            with(sharedPref.edit()) {
                putBoolean("use_toast", b)
                apply()
            }
        }
        val notificationSwitch = findViewById<Switch>(R.id.notification_switch)
        if (!validPermission) notificationSwitch.isEnabled = false
        notificationSwitch.isChecked = sharedPref.getBoolean("use_notification", true)
        notificationSwitch.setOnCheckedChangeListener { _, b: Boolean ->
            with(sharedPref.edit()) {
                putBoolean("use_notification", b)
                apply()
            }
        }
    }
}
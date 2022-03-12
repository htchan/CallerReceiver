package com.htchan.callreceiver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.pm.PackageManager

import android.content.ComponentName
import android.graphics.Color


class MainActivity : AppCompatActivity() {
    private val permissionUtils = PermissionUtils(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionUtils.handlePermission(this)
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
    }

    override fun onResume() {
        super.onResume()
        permissionUtils.handlePermission(this)
    }

    fun renderEnableButton() {
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
}
package com.htchan.callreceiver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.pm.PackageManager

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.widget.EditText
import android.widget.Switch
import com.htchan.callreceiver.helper.CallerHintHelper
import com.htchan.callreceiver.helper.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher


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
        setupTestArea()
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
            receiverText.text = getString(R.string.receiver_enabled)
            permissionText.setTextColor(getColor(R.color.design_default_color_on_primary))
            receiverText.setTextColor(getColor(R.color.design_default_color_on_primary))
            button.text = getString(R.string.disable)
            button.setOnClickListener {
                this.packageManager.setComponentEnabledSetting(
                    receiverName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                renderEnableButton()
                renderSwitches()
            }
        } else {
            permissionText.text = getString(R.string.permission_grant)
            permissionText.setTextColor(getColor(R.color.design_default_color_on_primary))
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
                renderSwitches()
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

    private fun setupTestArea() {
        val testPhoneNumberInput = findViewById<EditText>(R.id.test_phone_number)
        val sendTestButton = findViewById<Button>(R.id.send_test_button)
        val testResultView = findViewById<TextView>(R.id.test_result)

        sendTestButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Log.e("callreceiver", testPhoneNumberInput.text.toString())
                val result = CallerHintHelper().map(testPhoneNumberInput.text.toString())
                Log.e("callreceiver", "result: $result")
                runOnUiThread {
                    testResultView.text = result
                }
            }
        }
    }
}
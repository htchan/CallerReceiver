package com.htchan.callreceiver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.text.Editable
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputEditText
import com.htchan.callreceiver.helper.CallerHintHelper
import com.htchan.callreceiver.helper.ImageGetter
import com.htchan.callreceiver.helper.PermissionHelper
import com.htchan.callreceiver.helper.PowerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val permissionUtils = PermissionHelper(this)
    private lateinit var sharedPref: SharedPreferences

    companion object {
        const val QUERY_PHONE_NUMBER = "phone_number"

        fun newIntent(context: Context, phoneNumber: String) =
            Intent(context, MainActivity::class.java).apply {
                // Intent.FLAG_ACTIVITY_SINGLE_TOP
                // Clear all history, click back will close App
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(QUERY_PHONE_NUMBER, phoneNumber)
            }
    }

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
        renderCallerQueryField()
        setupTestArea(intent.getStringExtra(QUERY_PHONE_NUMBER))
    }

    override fun onResume() {
        super.onResume()
        permissionUtils.handlePermission(this)
        renderEnableButton()
        renderSwitches()
        renderCallerQueryField()
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
            if (!sharedPref.contains("caller_query")) {
                putString("caller_query", "https://hkjunkcall.com/?ft={phone_number}")
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

        val toastWarning = findViewById<TextView>(R.id.toast_warning)
        val powerManager = this.getSystemService(POWER_SERVICE) as PowerManager
        if (PowerHelper(applicationContext).isPowerSaving()) {
            toastWarning.visibility = View.VISIBLE
            toastWarning.text = "Toast Alert does not work when power saving mode is on"
            toastSwitch.isEnabled = false
        } else {
            toastWarning.visibility = View.INVISIBLE
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

    private fun renderCallerQueryField() {
        val callerQueryInput = findViewById<TextInputEditText>(R.id.caller_query_input)

        callerQueryInput.setText(sharedPref.getString("caller_query", "https://hkjunkcall.com/?ft={phone_number}"))
        callerQueryInput.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                with(sharedPref.edit()) {
                    val callerQueryLink = (view as TextInputEditText).text.toString()
                    
                    putString("caller_query", callerQueryLink)
                    apply()
                }
            }
        }
    }

    private fun setupTestArea(queryPhoneNumber: String?) {
        val phoneNumberInput = findViewById<TextInputEditText>(R.id.phone_number_input)
        val sendTestButton = findViewById<ImageButton>(R.id.send_test_button)
        val testResultView = findViewById<TextView>(R.id.test_result)

        sendTestButton.setOnClickListener {
            applyLoading(testResultView, sendTestButton)
            CoroutineScope(Dispatchers.IO).launch {
                Log.e("callreceiver", phoneNumberInput.text.toString())
                val result = CallerHintHelper().map(applicationContext, phoneNumberInput.text.toString())
                Log.e("callreceiver", "result: $result")
                runOnUiThread {
                    Log.d("check result", result)
                    applyResultHTML(testResultView, sendTestButton, result)
                }
            }
        }

        if (queryPhoneNumber != null) {
            phoneNumberInput.setText(queryPhoneNumber)
            sendTestButton.callOnClick()
        }
    }

    private fun applyLoading(view: TextView, btn: ImageButton) {
        view.text = "Loading ..."
        btn.isEnabled = false
    }

    private fun applyResultHTML(view: TextView, btn: ImageButton, text: String) {
        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, view)

        // to enable image/link clicking
        view.movementMethod = LinkMovementMethod.getInstance()

        view.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, null)
        btn.isEnabled = true
    }

    private fun applyResultText(view: TextView, text: String) {
        view.text = text
    }
}
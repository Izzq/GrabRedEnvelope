package com.carlos.cutils.base.activity

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

/**
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2019/2/22.
 */
open class CBaseAccessibilityActivity : CBaseActivity() {

    private lateinit var accessibilityManager: AccessibilityManager
    private lateinit var accessibilityServiceListeners: AccessibilityServiceListeners
    private lateinit var accessibilityServiceName: String

    interface AccessibilityServiceListeners {

        fun updateStatus(boolean: Boolean)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::accessibilityManager.isInitialized)
            accessibilityManager.removeAccessibilityStateChangeListener(
                mAccessibilityStateChangeListener
            )
    }

    fun setAccessibilityServiceName(accessibilityServiceName: String) {
        this.accessibilityServiceName = accessibilityServiceName
    }

    fun addAccessibilityServiceListener(
        accessibilityServiceListeners: AccessibilityServiceListeners,
        accessibilityServiceName: String
    ) {
        this.accessibilityServiceListeners = accessibilityServiceListeners
        this.accessibilityServiceName = accessibilityServiceName
        accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener(mAccessibilityStateChangeListener)
    }

    var mAccessibilityStateChangeListener: AccessibilityManager.AccessibilityStateChangeListener =
        AccessibilityManager.AccessibilityStateChangeListener {
            accessibilityServiceListeners.updateStatus(checkStatus())
        }

    fun checkStatus(): Boolean {
        val accessibilityServiceInfoList =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityServiceInfoList) {
            if (info.id == accessibilityServiceName) {
                return true
            }
        }
        return false
    }


    fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return !TextUtils.isEmpty(enabledServices) && enabledServices.contains(service.name)
    }


}

package com.carlos.grabredenvelope.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

object AccessibilityUtils {


    /** 检查无障碍是否开启 */
    fun isAccessibilityEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val serviceId = "${context.packageName}/${serviceClass.simpleName}"
        return !TextUtils.isEmpty(enabledServices) && enabledServices.contains(serviceId)
    }

    /** 跳转无障碍设置页 */
    fun goToAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}
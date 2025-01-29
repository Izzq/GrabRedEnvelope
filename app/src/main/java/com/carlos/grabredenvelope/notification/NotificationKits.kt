package com.carlos.grabredenvelope.notification

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.carlos.cutils.util.LogUtils


object NotificationKits {

    private var isStartPermanentService = false

    /**
     * 开启常驻通知栏
     *
     * @param context
     */
    fun startPermanentNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !isGrantedNotifications(context)
        ) {
            return
        }
        try {
            if (isStartPermanentService) return
            isStartPermanentService = true

            val notification = PermanentNotification.showPermanentNotification(context)
            if (notification != null) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, PermanentService::class.java)
                )
            } else {
            }
        } catch (e: Exception) {
            LogUtils.e("permanentPush error:${e.message}")
            //启动服务失败
        }
    }


    /**是否打开通知栏权限*/
    private fun isGrantedNotifications(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }


}
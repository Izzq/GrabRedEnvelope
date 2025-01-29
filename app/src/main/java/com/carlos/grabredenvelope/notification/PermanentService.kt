package com.carlos.grabredenvelope.notification

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


class PermanentService : Service() {
    override fun onBind(intent: Intent?): IBinder {
        return PermanentNotificationBinder()
    }

    /**
     * 向Activity传递数据的纽带
     */
    inner class PermanentNotificationBinder : Binder() {
        /**
         * 获取当前service对象
         *
         */
        val service: Service = this@PermanentService
    }

    override fun onCreate() {
        super.onCreate()
        startMyForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMyForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startMyForeground() {
        try {
//                val notificationId = packageName.length + 10086
            startForeground(
                Constant.RESIDENT_NOTIFICATION_ID,
                PermanentNotification.showPermanentNotification(this)
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
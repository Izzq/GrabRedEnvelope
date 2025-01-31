package com.carlos.grabredenvelope.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.activity.MainActivity


object PermanentNotification {

    private var manager: NotificationManager? = null
    private var remoteViews: RemoteViews? = null

    // 是否正在显示常驻通知栏，如果正在展示当定时推送时需要刷新红点
    var isShowingPermanent = false

    /**
     * 展示Notification
     * @return
     */
    fun showPermanentNotification(
        context: Context,
    ): Notification? {
        try {
            if (manager == null) {
                manager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                isShowingPermanent = true
            }

            val title = context.getString(R.string.ntf_resident_title)
            val content = context.getString(R.string.ntf_resident_content)


            // remoteView公用的话会出现TransactionTooLargeException异常
            remoteViews = RemoteViews(
                context.packageName,
                R.layout.notification_resident_open
            )

            remoteViews?.setTextViewText(R.id.tv_title, title)
            remoteViews?.setTextViewText(R.id.tv_content, content)

            val norIntent = PendingIntent.getActivity(
                context, 101010,
                Intent(context, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    it.putExtra("click_flag", 0)
                }, getPendingIntentFlags()
            )

            remoteViews?.setOnClickPendingIntent(R.id.rl_info, norIntent)

            val channelId = "residentNotificationId100"
            val channelName = "residentNotification100"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    importance
                )
                manager?.createNotificationChannel(channel)
            }

            val sIcResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                R.mipmap.ic_launcher//不设置小图标可能会不显示
            } else {
                R.mipmap.ic_launcher
            }
            val builder = NotificationCompat.Builder(context, channelId)
                .setCustomContentView(remoteViews)
                .setCustomHeadsUpContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setContent(remoteViews)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setGroup(context.packageName)

            try {
                builder.setSmallIcon(sIcResId)
            } catch (e: Exception) {

            }

            val notification = builder.build()
            manager?.notify(Constant.RESIDENT_NOTIFICATION_ID, notification)
            return notification
        } catch (t: Throwable) {
            LogUtils.e("常驻通知栏加载异常：${t.message}")
        }
        return null
    }

    /**
     * 获取 PendingIntent 的flags（例如：PendingIntent.getBroadcast 等的 flags）
     *
     * @return
     */
    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
}
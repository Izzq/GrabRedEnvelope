package com.carlos.grabredenvelope.float_windows

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.blankj.utilcode.util.VibrateUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.services.wechat.WechatService

class FloatingWindowService : Service() {

    companion object {
        var isRunningService = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var toggleButton: ImageView

    // 用于存储当前悬浮窗的 X 和 Y 坐标
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        isRunningService = true

        // 创建悬浮窗
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window_layout, null)

        toggleButton = floatingView.findViewById(R.id.iv_open_state)
        toggleButton.setImageResource(if (WechatService.isSwitchOnState()) R.mipmap.control_open_state else R.mipmap.control_close_state)
        toggleButton.setOnClickListener {
            if (WechatService.isRunningServiceState()) {
                WechatService.setSwitchOnState(!WechatService.isSwitchOnState())
                toggleButton.setImageResource(if (WechatService.isSwitchOnState()) R.mipmap.control_open_state else R.mipmap.control_close_state)
                VibrateUtils.vibrate(100)
            }
        }

        // 悬浮窗的布局参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // 将悬浮窗添加到 WindowManager
        windowManager.addView(floatingView, params)


        // 监听触摸事件来实现悬浮窗拖动
        floatingView.setOnTouchListener { v, event ->
            val params = v.layoutParams as WindowManager.LayoutParams
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录初始触摸位置
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    // 计算悬浮窗新的位置
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    // 更新悬浮窗的坐标
                    params.x = initialX + dx
                    params.y = initialY + dy

                    // 更新悬浮窗位置
                    windowManager.updateViewLayout(floatingView, params)
                }

                MotionEvent.ACTION_UP -> {
                    // 在松开手指时更新位置
                    // 你可以在这里进行一些额外的逻辑，比如保存位置等
                }
            }
            true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunningService = false
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}

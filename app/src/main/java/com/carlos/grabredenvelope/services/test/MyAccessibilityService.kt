package com.carlos.grabredenvelope.services.test

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.grabredenvelope.utils.LogUtil
import com.carlos.grabredenvelope.websocket.GsonHelper
import com.carlos.grabredenvelope.websocket.WebSend
import com.carlos.grabredenvelope.websocket.WsClient
import org.json.JSONObject

class MyAccessibilityService : AccessibilityService() {

    companion object {
        lateinit var instance: MyAccessibilityService
    }

    //WebSocket Client
    private var ws: WsClient? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        LogUtil.d("✅ WebSocket onServiceConnected")
        // 启动 WebSocket Client
        ws = WsClient(this)
        ws?.connect()
    }

    //    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val cls = event.className?.toString() ?: return

        // 微信聊天界面
        if (cls.contains("LauncherUI") || cls.contains("ChattingUI")) {
            val root = rootInActiveWindow ?: return
            findAndClickRedPacket(root)
        }

        // 红包详情页面
        if (cls.contains("LuckyMoneyReceiveUI")) {
            clickOpenButton(rootInActiveWindow)
        }
    }


    private fun findAndClickRedPacket(node: AccessibilityNodeInfo) {
        val redPackets = node.findAccessibilityNodeInfosByText("微信红包")
        for (packet in redPackets) {
            if (packet.isClickable) {
                notifyPC(GsonHelper.gson.toJson(WebSend(action = "command", info = "click point")))
            }
        }
    }

    private fun clickOpenButton(node: AccessibilityNodeInfo?) {
        notifyPC(GsonHelper.gson.toJson(WebSend(action = "command", info = "click botton")))
    }

    private fun notifyPC(info: String) {
        LogUtil.d("➡️ 已通知 PC: $info")
        ws?.send(info)
    }


    fun clickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        nodes.forEach {
            if (it.isClickable) {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                LogUtil.d("✅ 点击文本: $text")
                return true
            }
        }
        LogUtil.w("未找到文本节点: $text")
        return false
    }

    fun back() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        LogUtil.d("✅ 返回操作")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        val path =
            Path().apply { moveTo(x1.toFloat(), y1.toFloat()); lineTo(x2.toFloat(), y2.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200)).build()
        dispatchGesture(gesture, null, null)
        LogUtil.d("✅ 滑动操作: ($x1,$y1) -> ($x2,$y2)")
    }

    fun handleCommand(jsonStr: String) {
        LogUtil.json(jsonStr)
        val obj = JSONObject(jsonStr)
        when (obj.getString("action")) {
            "tap_text" -> clickByText(obj.getString("text"))
            "back" -> back()
            "swipe" -> {
                val from = obj.getJSONArray("from")
                val to = obj.getJSONArray("to")
                swipe(from.getInt(0), from.getInt(1), to.getInt(0), to.getInt(1))
            }
        }
    }
}

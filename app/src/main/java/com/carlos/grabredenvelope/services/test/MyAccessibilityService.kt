package com.carlos.grabredenvelope.services.test

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.websocket.ICommandService
import com.carlos.grabredenvelope.websocket.GsonHelper
import com.carlos.grabredenvelope.websocket.WebSend
import com.carlos.grabredenvelope.websocket.WsClient
import org.json.JSONObject

class MyAccessibilityService : AccessibilityService(), ICommandService {

    companion object {
        lateinit var instance: MyAccessibilityService
    }

    //WebSocket Client
    private var ws: WsClient? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        LogUtils.d("✅ WebSocket onServiceConnected")
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

                // 获取控件的边界
                val bounds = Rect()
                node.getBoundsInScreen(bounds)

                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
                val x1 = bounds.left // 控件左上角 X 坐标
                val y1 = bounds.top // 控件左上角 Y 坐标
                val x2 = bounds.right // 控件右下角 X 坐标
                val y2 = bounds.bottom // 控件右下角 Y 坐标

                val xy = "(x1=$x1,y1=$y1),(x2=$x2,y2=$y2)"

                sendWsMessage(
                    GsonHelper.gson.toJson(
                        WebSend(
                            action = "command",
                            info = "click point"
                        )
                    )
                )
            }
        }
    }

    private fun clickOpenButton(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // 获取控件的边界
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
        val x1 = bounds.left // 控件左上角 X 坐标
        val y1 = bounds.top // 控件左上角 Y 坐标
        val x2 = bounds.right // 控件右下角 X 坐标
        val y2 = bounds.bottom // 控件右下角 Y 坐标

        val xy = "(x1=$x1,y1=$y1),(x2=$x2,y2=$y2)"

        LogUtils.d("clickOpenButton: $xy")

        sendWsMessage(GsonHelper.gson.toJson(WebSend(action = "command", info = xy)))
    }

    override fun sendWsMessage(jsonStr: String) {
        LogUtils.d("➡️ 已通知 PC: $jsonStr")
        ws?.send(jsonStr)
    }


    fun clickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        nodes.forEach {
            if (it.isClickable) {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                LogUtils.d("✅ 点击文本: $text")
                return true
            }
        }
        LogUtils.w("未找到文本节点: $text")
        return false
    }

    fun back() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        LogUtils.d("✅ 返回操作")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        val path =
            Path().apply { moveTo(x1.toFloat(), y1.toFloat()); lineTo(x2.toFloat(), y2.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200)).build()
        dispatchGesture(gesture, null, null)
        LogUtils.d("✅ 滑动操作: ($x1,$y1) -> ($x2,$y2)")
    }

    override fun handleWsCommand(jsonStr: String) {
        LogUtils.json(jsonStr)
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

package com.carlos.grabredenvelope.services.test

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.grabredenvelope.utils.LogUtil
import com.carlos.grabredenvelope.websocket.WebSocketConst
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class RedPacketService : AccessibilityService() {

    private var ws: WebSocket? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url(WebSocketConst.SERVER).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtil.d("✅ WebSocket 已连接")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                LogUtil.d("⬅️ 收到 PC 消息: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                LogUtil.d("❌ WebSocket 连接失败: ${t.message}")
            }
        })
    }

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
                packet.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                // 获取控件的边界
                val bounds = Rect()
                packet.getBoundsInScreen(bounds)

                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
                val x1 = bounds.left // 控件左上角 X 坐标
                val y1 = bounds.top // 控件左上角 Y 坐标
                val x2 = bounds.right // 控件右下角 X 坐标
                val y2 = bounds.bottom // 控件右下角 Y 坐标


//                notifyPC("红包: (x1=$x1,y1=$y1),(x2=$x2,y2=$y2)")
                notifyPC("红包aaaa")
            }
        }
    }

    private fun clickOpenButton(node: AccessibilityNodeInfo?) {
        if (node == null) return
        val openBtn = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d02")
        openBtn?.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun notifyPC(info: String) {
        ws?.send("""{"action":"red_packet","info":"$info"}""")
        LogUtil.d("➡️ 已通知 PC: $info")
    }

    override fun onInterrupt() {}


}

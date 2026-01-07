package com.carlos.grabredenvelope.websocket

import okhttp3.*
import android.os.Build
import com.carlos.grabredenvelope.services.test.MyAccessibilityService
import com.carlos.grabredenvelope.utils.LogUtil

class WsClient(private val service: MyAccessibilityService) {

    private val client = OkHttpClient()
    private var ws: WebSocket? = null

    fun connect() {
        // 自动选择地址
        val serverUrl = if (Build.FINGERPRINT.contains("generic")||Build.FINGERPRINT.contains("google")) {
            // 模拟器
            "ws://10.0.2.2:8765"
        } else {
            // TODO: 注意 真机，修改为局域网 PC IP
            "ws://192.168.1.103:8765"
        }

        val request = Request.Builder().url(serverUrl).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtil.d("✅ WebSocket 已连接")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                LogUtil.d("⬅️ 收到消息")
                service.handleCommand(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                LogUtil.e("❌ WebSocket 连接失败", throwable = t)
                Thread.sleep(2000)
                connect() // 自动重连
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                LogUtil.e("❌ WebSocket 连接 Closed , reason=${reason}")
            }
        })
    }
}

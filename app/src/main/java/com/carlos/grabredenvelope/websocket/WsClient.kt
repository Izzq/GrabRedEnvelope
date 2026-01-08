package com.carlos.grabredenvelope.websocket

import android.os.Build
import com.carlos.grabredenvelope.services.test.MyAccessibilityService
import com.carlos.grabredenvelope.utils.LogUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WsClient(private val service: MyAccessibilityService) {

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private var heartBeatRunnable: Runnable? = null

    @Volatile
    private var isConnected = false

//    private val heartBeatInterval: Long = 5 // 心跳间隔，单位：秒
    private val heartBeatInterval: Long = 10 // 心跳间隔，单位：秒

    fun connect() {
        // 自动选择地址
        val serverUrl =
            if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("google")) {
                // 模拟器
                "ws://10.0.2.2:8765"
            } else {
                // TODO: 注意 真机，修改为局域网 PC IP
                WebSocketConst.SERVER
            }

        val request = Request.Builder().url(serverUrl).build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtil.d("✅ WebSocket 已连接")
                isConnected = true
                startHeartBeat()  // 开始发送心跳
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                LogUtil.d("⬅️ 收到消息: $text")
                service.handleCommand(text)

                // 如果是心跳响应，也可以处理
                if (text.contains("heartbeat_response")) {
                    LogUtil.d("💓 收到服务器心跳响应")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                LogUtil.e("❌ WebSocket 连接失败", throwable = t)
                isConnected = false
                Thread.sleep(2000)
                connect() // 自动重连
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                LogUtil.e("❌ WebSocket 连接 Closed , reason=${reason}")
                isConnected = false
            }
        })
    }

    fun send(content: String) {
        if (content.isNotEmpty()) {
            ws?.send(content)
        }
    }

    // 开始定时发送心跳消息
    private fun startHeartBeat() {
        heartBeatRunnable = Runnable {
            while (isConnected) {
                try {
                    // 发送心跳消息
                    ws?.send("{\"action\":\"heartbeat\"}")
                    LogUtil.d("➡️ 发送心跳")
                    Thread.sleep(TimeUnit.SECONDS.toMillis(heartBeatInterval)) // 每隔 heartBeatInterval 秒发送一次心跳
                } catch (e: InterruptedException) {
                    LogUtil.e("❌ 心跳发送失败: ${e.message}")
                }
            }
        }
        Thread(heartBeatRunnable).start()
    }

    // 停止心跳
    fun stopHeartBeat() {
        heartBeatRunnable?.let { Thread(it).interrupt() }
    }

    // 关闭 WebSocket 连接
    fun close() {
        ws?.close(1000, "Normal closure")
        isConnected = false
        stopHeartBeat() // 停止心跳
    }
}

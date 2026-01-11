package com.carlos.grabredenvelope.websocket

import android.os.Build
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.MyApplication
import com.carlos.grabredenvelope.event.WsStateEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WsClient(private val service: ICommandService) {

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private var heartBeatRunnable: Runnable? = null


    companion object {

        @Volatile
        private var isConnected = false

        @Volatile
        private var state = WsState.NONE

        /**获取连接状态*/
        fun hasConnected(): Boolean {
            return state == WsState.CONNECTED
        }

    }


    private val heartBeatInterval: Long = 10 // 心跳间隔，单位：秒

    fun connect() {
        // 自动选择地址
        val serverUrl =
            if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("google")) {
                // 模拟器
                "ws://10.0.2.2:8765"
            } else {
                // TODO: 注意 真机，修改为局域网 PC IP
                WebSocketConst.getWsServer()
            }

        LogUtils.d("[ws] ✅ WebSocket 开始连接 $serverUrl")

        state = WsState.CONNECTING
        MyApplication.instance.wsEvent.postValue(WsStateEvent(state))

        val request = Request.Builder().url(serverUrl).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtils.d("[ws] ✅ WebSocket 已连接")
                state = WsState.CONNECTED
                isConnected = true
                MyApplication.instance.wsEvent.postValue(WsStateEvent(state))
                startHeartBeat()  // 开始发送心跳
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 如果是心跳响应，也可以处理
                if (text.contains("heartbeat")) {
                    LogUtils.d("[ws] 💓 收到服务器心跳消息")
                } else {
                    LogUtils.d("[ws] ⬅️ 收到消息: $text")
                    service.handleWsCommand(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                LogUtils.e("[ws] ❌ WebSocket 连接失败 ${t.message}")
                state = WsState.NONE
                isConnected = false
                MyApplication.instance.wsEvent.postValue(WsStateEvent(state))
                Thread.sleep(2000)
                connect() // 自动重连
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                LogUtils.e("[ws] ❌ WebSocket 连接 Closed , reason=${reason}")
                state = WsState.NONE
                isConnected = false
                MyApplication.instance.wsEvent.postValue(WsStateEvent(state))
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
                    ws?.send(GsonHelper.gson.toJson(WebSend(action = "heartbeat", info = "")))
                    LogUtils.d("[ws] ➡️ 发送心跳")
                    Thread.sleep(TimeUnit.SECONDS.toMillis(heartBeatInterval)) // 每隔 heartBeatInterval 秒发送一次心跳
                } catch (e: InterruptedException) {
                    LogUtils.e("[ws] ❌ 心跳发送失败: ${e.message}")
                }
            }
        }
        Thread(heartBeatRunnable).start()
    }

    // 停止心跳
    private fun stopHeartBeat() {
        heartBeatRunnable?.let { Thread(it).interrupt() }
    }

    // 关闭 WebSocket 连接
    fun close() {
        try {
            ws?.close(1000, "Normal closure")
            isConnected = false
            stopHeartBeat() // 停止心跳
            state = WsState.NONE
            MyApplication.instance.wsEvent.postValue(WsStateEvent(state))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

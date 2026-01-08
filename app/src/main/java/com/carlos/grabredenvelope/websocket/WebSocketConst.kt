package com.carlos.grabredenvelope.websocket

object WebSocketConst {

    fun getWsServer(): String {
        return "ws://$IP:8765"
    }

    var IP = "192.168.1.104"

    fun setNewIP(ip: String) {
        IP = ip
    }

}
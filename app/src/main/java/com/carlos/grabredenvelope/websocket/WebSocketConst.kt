package com.carlos.grabredenvelope.websocket

object WebSocketConst {

    fun getWsServer(): String {
        return "ws://$IP:8765"
    }

    var IP = "192.168.191.97"

    fun setNewIP(ip: String) {
        IP = ip
    }

}
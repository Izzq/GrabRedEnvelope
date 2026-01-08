package com.carlos.grabredenvelope.websocket

interface ICommandService {

    fun handleWsCommand(jsonStr: String)


    fun sendWsMessage(jsonStr: String)

}
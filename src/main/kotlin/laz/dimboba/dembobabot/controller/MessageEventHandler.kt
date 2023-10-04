package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent

interface MessageEventHandler {
    val commandsUpperCase: List<String>
    suspend fun handleMessage(messageEvent: MessageCreateEvent, args: List<String>)
}
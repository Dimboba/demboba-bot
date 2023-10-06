package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent

interface MessageEventHandler {
    suspend fun isCommandAcceptable(command: String, messageEvent: MessageCreateEvent): Boolean
    suspend fun handleMessage(messageEvent: MessageCreateEvent, args: List<String>)
}
package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent

class MessageHandler(
    private val eventHandlers: List<MessageEventHandler>
) {
    fun handleMessage(messageCreateEvent: MessageCreateEvent){
        eventHandlers.forEach { _ ->
            handleMessage(messageCreateEvent)
        }
    }
}
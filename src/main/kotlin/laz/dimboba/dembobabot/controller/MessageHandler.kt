package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.exceptions.UnknownCommandException

class MessageHandler(
    private val eventHandlers: List<MessageEventHandler>
) {
    suspend fun handleMessage(messageCreateEvent: MessageCreateEvent){

        var errors = 0;

        for(handler in eventHandlers) {
            try {
                handler.handleMessage(messageCreateEvent)
            } catch (ex: UnknownCommandException) {
                errors++;
            }
        }
        if(errors >= eventHandlers.size){
            throw UnknownCommandException("Unknown command ${messageCreateEvent.message.content}")
        }
    }
}
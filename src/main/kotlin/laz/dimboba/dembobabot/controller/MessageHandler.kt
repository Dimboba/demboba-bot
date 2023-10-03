package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.exceptions.UnknownCommandException

private val logger = KotlinLogging.logger {  }

class MessageHandler(
    private val eventHandlers: List<MessageEventHandler>
) {
    init {
        logger.info {
            "MessageHandler is started"
        }
        var handlersNames = ""
        for(eventHandler: MessageEventHandler in eventHandlers) {
            handlersNames += eventHandler::class.simpleName
            if(eventHandler !== eventHandlers.last()) {
                handlersNames += ", "
            }
        }
        logger.info {
            "Working MessageEventHandlers: $handlersNames"
        }
    }
    suspend fun handleMessage(messageCreateEvent: MessageCreateEvent){

        var errors = 0;

        logger.info {
            "Handling message: ${messageCreateEvent.message.content}"
        }

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
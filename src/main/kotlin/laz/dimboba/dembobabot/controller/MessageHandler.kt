package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import java.util.*

private val logger = KotlinLogging.logger { }

class MessageHandler(
    private val eventHandlers: List<MessageEventHandler>,
    private val commandChar: Char = '!'
) {
    init {
        logger.info {
            "MessageHandler is started"
        }
        var handlersNames = ""
        for (eventHandler: MessageEventHandler in eventHandlers) {
            handlersNames += eventHandler::class.simpleName
            if (eventHandler !== eventHandlers.last()) {
                handlersNames += ", "
            }
        }
        logger.info {
            "Working MessageEventHandlers: $handlersNames"
        }
    }

    suspend fun handleMessage(messageCreateEvent: MessageCreateEvent) {
        logger.info {
            "Handling message: ${messageCreateEvent.message.content}"
        }
        val args = parseCommand(messageCreateEvent.message.content)
        for (handler in eventHandlers) {
            println(handler.commandsUpperCase)
            if (handler.commandsUpperCase.contains(args[0].substring(1, args[0].length))) {
                handler.handleMessage(
                    messageCreateEvent,
                    args
                )
                return
            }
        }
        throw UnknownCommandException("Unknown command ${messageCreateEvent.message.content}")
    }

    private fun parseCommand(messageContent: String) : List<String> {
        val content = messageContent.replace("-", "_")
            .uppercase(Locale.getDefault())
            .split(" ")
        if(content.isEmpty())
            throw NotACommandMessageException("Message: there is no commands")

        if (content[0].length < 2 || content[0][0] != commandChar)
            throw NotACommandMessageException("Message: \"${content[0]}\" is not a command")

        return content
    }
}
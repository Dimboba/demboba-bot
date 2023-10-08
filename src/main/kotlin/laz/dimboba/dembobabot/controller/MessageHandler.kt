package laz.dimboba.dembobabot.controller

import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val logger = KotlinLogging.logger { }

@Singleton
class MessageHandler : KoinComponent {
    private val eventHandlers: List<MessageEventHandler> by lazy { getKoin().getAll<MessageEventHandler>() }

    private val commandChar: Char = '!'
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
            if (handler.isCommandAcceptable(
                args[0].substring(1, args[0].length),
                messageCreateEvent
            )) {
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
        val content = messageContent.split(" ").toMutableList()
        if(content.isEmpty())
            throw NotACommandMessageException("Message: there is no commands")

        content[0] = content[0].replace("-", "_")

        if (content[0].length < 2 || content[0][0] != commandChar)
            throw NotACommandMessageException("Message: \"${content[0]}\" is not a command")

        return content
    }
}
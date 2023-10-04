package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.impl.enums.SimpleCommand
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import java.util.*


//TODO: ErrorHandler

class SimpleMessageEventHandler : MessageEventHandler {
    private var currMessage: Message? = null
    override val commandsUpperCase: List<String> =
        SimpleCommand.entries.map {
            command -> command.name
        }
    override suspend fun handleMessage(messageEvent: MessageCreateEvent, args: List<String>) {
        currMessage = messageEvent.message

        val command = args[0].substring(
            1,
            args[0].length
        ).uppercase(Locale.getDefault())

        //TODO: nicknames to battle tag from json through map for best pies on server

        SimpleCommand.valueOf(command)
            .exec(
                args,
                currMessage!!
            )
    }

}
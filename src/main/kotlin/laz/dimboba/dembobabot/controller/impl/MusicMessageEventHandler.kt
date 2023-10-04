package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.impl.enums.MusicCommand
import laz.dimboba.dembobabot.controller.impl.enums.SimpleCommand
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.voice.TrackScheduler
import java.util.*

class MusicMessageEventHandler(
    private val trackScheduler: TrackScheduler
) : MessageEventHandler {
    private var currMessage: Message? = null
    override val commandsUpperCase: List<String> =
        MusicCommand.entries.map {
                command -> command.name
        }

    override suspend fun handleMessage(messageEvent: MessageCreateEvent, args: List<String>) {
        currMessage = messageEvent.message

        val command = args[0].substring(
            1,
            args[0].length
        ).uppercase(Locale.getDefault())

        MusicCommand.valueOf(command)
            .exec(
                trackScheduler,
                args,
                currMessage!!
            )

    }

}
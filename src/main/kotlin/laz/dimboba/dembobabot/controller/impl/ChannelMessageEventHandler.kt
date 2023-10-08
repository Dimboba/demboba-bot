package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.channel.ChannelHandler
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.impl.enums.ChannelCommand
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@Singleton
@Named("ChannelMessageEventHandler")
class ChannelMessageEventHandler : MessageEventHandler, KoinComponent {

    private val channelHandler: ChannelHandler by inject()
    private var currMessage: Message? = null
    override suspend fun isCommandAcceptable(command: String, messageEvent: MessageCreateEvent): Boolean {
        try {
            ChannelCommand.valueOf(command.uppercase(Locale.getDefault()))
        } catch (_: IllegalArgumentException) {
            return false
        }
        return true
    }

    override suspend fun handleMessage(messageEvent: MessageCreateEvent, args: List<String>) {
        currMessage = messageEvent.message

        val command = args[0].substring(
            1,
            args[0].length
        ).uppercase(Locale.getDefault())

        ChannelCommand.valueOf(command)
            .exec(
                channelHandler,
                args
            )
    }
}
package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.impl.enums.MusicCommand
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.TrackScheduler
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.*

@Singleton
@Named("MusicMessageEventHandler")
class MusicMessageEventHandler : MessageEventHandler, KoinComponent {

    private val trackScheduler: TrackScheduler by inject()
    private val musicMessageChannel: MessageChannel by inject(named("MusicTextChannel"))


    private var currMessage: Message? = null

    override suspend fun isCommandAcceptable(command: String, messageEvent: MessageCreateEvent): Boolean {
        try {
            MusicCommand.valueOf(command.uppercase(Locale.getDefault()))
        } catch (_: IllegalArgumentException) {
            return false
        }
        if (messageEvent.message.channel != musicMessageChannel) {
            throw UnknownCommandException("Music should be played in other channel")
        }
        return true
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
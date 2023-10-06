package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.impl.enums.ChannelCommand
import laz.dimboba.dembobabot.controller.impl.enums.MusicCommand
import laz.dimboba.dembobabot.controller.impl.enums.SimpleCommand
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.PlayerEventListener
import laz.dimboba.dembobabot.voice.TrackScheduler
import java.lang.IllegalArgumentException
import java.util.*

class MusicMessageEventHandler (
    private val trackScheduler: TrackScheduler,
    private val messageChannel: MessageChannel
) : MessageEventHandler {

    init {
        val playerEventListener = PlayerEventListener(messageChannel)
        trackScheduler.addListener(playerEventListener)
    }

    private var currMessage: Message? = null

    override suspend fun isCommandAcceptable(command: String, messageEvent: MessageCreateEvent): Boolean {
        try {
            MusicCommand.valueOf(command.uppercase(Locale.getDefault()))
        } catch (_: IllegalArgumentException) {
            return false
        }
        if(messageEvent.message.channel != messageChannel) {
            throw UnknownCommandException("Music should be played in other channel")
        }
        val voiceChannel = messageEvent
            .message
            .getAuthorAsMemberOrNull()
            ?.getVoiceState()
            ?.getChannelOrNull() ?: throw CannotFindMemberException("There is no such member")
        //println(voiceChannel)
        //println(trackScheduler.voiceGuild)
        if (trackScheduler.voiceGuild != null &&
            voiceChannel.id != trackScheduler.voiceGuild?.id) {
            throw NotACommandMessageException("You must be in voice channel to do that")
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
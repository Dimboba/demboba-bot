package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.TrackScheduler
import java.util.*

class MusicMessageEventHandler (
    private val trackScheduler: TrackScheduler
): MessageEventHandler {

    private val commandChar: Char = '!'
    private var currMessage: Message? = null


    override suspend fun handleMessage(messageEvent: MessageCreateEvent) {



        currMessage = messageEvent.message

        val text = currMessage!!.content.split(" ")

        val keyword: String;
        try {
            keyword = text[0];
        } catch (ex: IndexOutOfBoundsException) {
            throw NotACommandMessageException("Message: there is no commands")
        }

        if (keyword.length < 2 || keyword[0] != commandChar)
            throw NotACommandMessageException("Message: \"$text\" is not a command")

        //TODO: nicknames to battle tag from json through map for best pies on server

        when (val command = keyword.substring(1, keyword.length).lowercase(Locale.getDefault())) {
            "play" -> playMusic(messageEvent.member, currMessage!!)
            "leave" -> trackScheduler.leave(currMessage!!)
            "pause" -> trackScheduler.pause(currMessage!!)
            "next" -> trackScheduler.nextSong(currMessage!!)
            "queue" -> trackScheduler.showQueue(currMessage!!)
            "clear" -> trackScheduler.emptyQueue(currMessage!!)

            else -> throw UnknownCommandException("Unknown command: \"$command\"")
        }
    }

    private suspend fun playMusic(member: Member?, message: Message) {

        val channel = member?.getVoiceState()?.getChannelOrNull()
            ?: throw CannotFindMemberException("There is no such member")


        trackScheduler.play(
            message,
            channel,
            message.content.removePrefix(commandChar + "play"))

    }

}
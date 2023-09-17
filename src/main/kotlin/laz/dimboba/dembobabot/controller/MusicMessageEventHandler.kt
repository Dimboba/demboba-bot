package laz.dimboba.dembobabot.controller

import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.MusicPlayer
import java.util.*

class MusicMessageEventHandler (
    private val musicPlayer: MusicPlayer
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
            "play" -> playMusic(text, messageEvent.member, currMessage!!)
            "leave" -> musicPlayer.leave(currMessage!!)
            "pause" -> musicPlayer.pause(currMessage!!)
            "next" -> musicPlayer.nextSong(currMessage!!)

            else -> throw UnknownCommandException("Unknown command: \"$command\"")
        }


    }

    private suspend fun playMusic(text: List<String>, member: Member?, message: Message) {

        val channel = member?.getVoiceState()?.getChannelOrNull()
            ?: throw CannotFindMemberException("There is no such member")


        musicPlayer.playYTSong(
            channel,
            message,
            message.content.removePrefix(commandChar + "play"))

    }

}
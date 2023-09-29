package laz.dimboba.dembobabot.controller.impl

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.channel.ChannelHandler
import laz.dimboba.dembobabot.channel.MessageChannelType
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import java.util.*

class ChannelMessageEventHandler(
    private val channelHandler: ChannelHandler,
    private val commandChar: Char = '!'
) : MessageEventHandler {

    private var currMessage: Message? = null

    override suspend fun handleMessage(messageEvent: MessageCreateEvent) {


        currMessage = messageEvent.message

        val text = currMessage!!.content.split(" ")
        println(text)
        val keyword: String;
        try {
            keyword = text[0];
        } catch (ex: IndexOutOfBoundsException) {
            throw NotACommandMessageException("Message: there is no commands")
        }

        if (keyword.length < 2 || keyword[0] != commandChar)
            throw NotACommandMessageException("Message: \"$text\" is not a command")


        when (val command = keyword.substring(1, keyword.length).lowercase(Locale.getDefault())) {
            "create-channel" -> createChannel(text)
            "delete-channel" -> TODO()
            "create-category" -> createCategory(text)
            "delete-category" -> TODO()
            else -> throw UnknownCommandException("Unknown command: \"$command\"")
        }
    }

    private suspend fun createChannel(text: List<String>) {
        channelHandler.createChannelIfNotExist(
            name = text[2],
            type = MessageChannelType.valueOf(text[1].uppercase(Locale.getDefault())),
            categoryName = if(text.size > 3) text[3] else null
        )
    }

    private suspend fun createCategory(text: List<String>) {
        channelHandler.createCategoryIfNotExist(text[1])
    }

    private suspend fun deleteChannel(text: List<String>) {
        channelHandler.deleteChannelIfExist(
            name = text[1]
        )
    }
}
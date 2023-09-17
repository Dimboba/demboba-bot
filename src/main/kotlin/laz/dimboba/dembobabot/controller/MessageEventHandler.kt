package laz.dimboba.dembobabot.controller

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

interface MessageEventHandler {
    suspend fun handleMessage(messageEvent: MessageCreateEvent)
}
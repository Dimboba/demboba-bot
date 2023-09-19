package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import laz.dimboba.dembobabot.controller.MessageEventHandler
import laz.dimboba.dembobabot.controller.MessageHandler
import laz.dimboba.dembobabot.controller.impl.MusicMessageEventHandler
import laz.dimboba.dembobabot.controller.impl.SimpleMessageEventHandler
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.TrackScheduler
import laz.dimboba.dembobabot.voice.VoiceConnectionsHandler

suspend fun main(args: Array<String>){

    val token: String = System.getenv("discord_token") ?: "null-token"

    val kord = Kord(token)
    val voiceConnectionsHandler = VoiceConnectionsHandler()
    val trackScheduler = TrackScheduler(voiceConnectionsHandler)

    val simpleMessageEventHandler: MessageEventHandler = SimpleMessageEventHandler()
    val musicMessageEventHandler: MessageEventHandler = MusicMessageEventHandler(trackScheduler)

    val messageHandler = MessageHandler(listOf(simpleMessageEventHandler, musicMessageEventHandler))
    //получение каналов и групп каналов
//    kord.guilds.first().channels.collect{
//        channel -> println(channel.name + "  " + channel.type)
//    }

//    val generalChannel = kord.guilds
//        .first()
//        .channels
//        .first { channel -> channel.name == "основной" || channel.name == "general" }
    //kord.createGuildChatInputCommand()

    kord.on<MessageCreateEvent> {

        // ignore other bots, even ourselves. We only serve humans here!
        if (message.author?.isBot != false) return@on

        println(message.content)

        try {
            messageHandler.handleMessage(this)
        } catch (ex: UnknownCommandException) {
            message.channel.createMessage(
                "В Политехе такому не учили :<"
            )
            return@on
        } catch (_: NotACommandMessageException) {
            return@on
        }

    }



    kord.login {
        // we need to specify this to receive the content of messages
        @OptIn(PrivilegedIntent::class)
        intents += Intent.GuildMembers

        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent

        @OptIn(PrivilegedIntent::class)
        intents += Intent.GuildVoiceStates
    }
}


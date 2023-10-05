package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.first
import laz.dimboba.dembobabot.channel.ChannelHandler
import laz.dimboba.dembobabot.channel.MessageChannelType
import laz.dimboba.dembobabot.controller.MessageHandler
import laz.dimboba.dembobabot.controller.impl.ChannelMessageEventHandler
import laz.dimboba.dembobabot.controller.impl.MusicMessageEventHandler
import laz.dimboba.dembobabot.controller.impl.SimpleMessageEventHandler
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.PlayerEventListener
import laz.dimboba.dembobabot.voice.TrackScheduler
import laz.dimboba.dembobabot.voice.VoiceConnectionsHandler

suspend fun main(args: Array<String>) {

    val token: String = System.getenv("discord_token") ?: "null-token"

    val kord = Kord(token)
    //TODO: better search for serverGuild
    val channelHandler = ChannelHandler(kord.guilds.first())
    val voiceConnectionsHandler = VoiceConnectionsHandler()
    val trackScheduler = TrackScheduler(voiceConnectionsHandler)

    val musicMessageChannel = channelHandler.getTextMessageChannelInstance(
        "Music"
    )
    val playerEventListener = PlayerEventListener(musicMessageChannel)
    trackScheduler.addListener(playerEventListener)

    val simpleMessageEventHandler = SimpleMessageEventHandler()
    val musicMessageEventHandler = MusicMessageEventHandler(trackScheduler)
    val channelMessageEventHandler = ChannelMessageEventHandler(channelHandler)
    val messageHandler = MessageHandler(
        listOf(
            simpleMessageEventHandler,
            musicMessageEventHandler,
            channelMessageEventHandler
        )
    )

    //получение каналов и групп каналов
//    kord.guilds.first().channels.collect{
    //     channel -> println(channel.name + "  " + channel.type)
//    }
//
//    val generalChannel = kord.guilds
//        .first()
//        .channels
//        .first { channel -> channel.name == "основной" || channel.name == "general" }
    //kord.createGuildChatInputCommand()

//    kord.guilds.collect {
//        guild -> println(guild.name + " " + guild.applicationId)
//    }


    kord.on<MessageCreateEvent> {

        // ignore other bots, even ourselves. We only serve humans here!
        if (message.author?.isBot != false) return@on

        println(message.content)

//        try {
//            if (message.content == "!create") {
//                channelHandler.createChannelIfNotExist("music", ChannelType.VOICE)
//            }
//        } catch (ex: GuildAlreadyExists) {
//            ex.message?.let { message.channel.createMessage(it) }
//        }

        try {
            messageHandler.handleMessage(this)
        } catch (ex: UnknownCommandException) {
            message.channel.createMessage(
                "В Политехе такому не учили :<"
            )
            return@on
        } catch (ex: Exception) {
            //error(ex.localizedMessage)
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


package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException

suspend fun main(args: Array<String>){

    val token: String = System.getenv("discord_token") ?: "null-token"

    println(token)

    val kord = Kord(token)

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

        try {
            //TODO: переделать в просто handle message
            message.channel.createMessage(
                handleMessage(message)
            )
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

    }
}


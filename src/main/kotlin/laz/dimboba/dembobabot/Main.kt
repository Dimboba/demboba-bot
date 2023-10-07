package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import laz.dimboba.dembobabot.channel.ChannelHandler
import laz.dimboba.dembobabot.controller.MessageHandler
import laz.dimboba.dembobabot.controller.impl.ChannelMessageEventHandler
import laz.dimboba.dembobabot.controller.impl.MusicMessageEventHandler
import laz.dimboba.dembobabot.controller.impl.SimpleMessageEventHandler
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.TrackScheduler
import laz.dimboba.dembobabot.voice.VoiceConnectionsHandler
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.*

var kord: Kord? = null
    get() = field
    set(value) {
        field = value
    }

private val logger = KotlinLogging.logger { }
suspend fun main(args: Array<String>) {

    val token: String = System.getenv("discord_token") ?: "null-token"

    kord = Kord(token)

    startKoin {
        printLogger(Level.INFO)

        modules(
            VoiceModel().module,
            OverwatchModel().module,
            ControllerModel().module,
            ChannelModel().module,
            MainModel().module
        )

    }

    //TODO: better search for serverGuild
    //TODO: create config for musicTextChannelId

    kord!!.on<MessageCreateEvent> {

        // ignore other bots, even ourselves. We only serve humans here!
        if (message.author?.isBot != false) return@on
        try {
            MessageHandler().handleMessage(this)
        } catch (ex: UnknownCommandException) {
            message.channel.createMessage(
                "В Политехе такому не учили :<"
            )
            return@on
        } catch (ex: Exception) {
            logger.error(ex) {}
            return@on
        }

    }

    kord!!.login {
        // we need to specify this to receive the content of messages
        @OptIn(PrivilegedIntent::class)
        intents += Intent.GuildMembers

        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent

        @OptIn(PrivilegedIntent::class)
        intents += Intent.GuildVoiceStates
    }
}


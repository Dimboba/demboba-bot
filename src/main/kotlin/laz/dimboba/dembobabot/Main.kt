package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.controller.CommandAction
import laz.dimboba.dembobabot.controller.MapMessageHandler
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.ksp.generated.module

/*
kord.createGlobalApplicationCommands {
        input("connect", "Connects to your channel")
        input("pause", "Pauses the player")
        input("stop", "Stops the player")
        input("leave", "Leaves the channel")
        input("play", "Starts playing a track") {
            string("query", "The query you want to play")
        }
    }
 */

private val logger = KotlinLogging.logger { }
suspend fun main(args: Array<String>) {

    startKoin {
        printLogger(Level.ERROR)
        modules(
            MainModule().module,
            VoiceModule().module,
            OverwatchModule().module,
            ControllerModule().module,
            ChannelModule().module,
            HelpModule().module
        )
    }

    val mapMessageHandler = getKoin().get<MapMessageHandler>()
    val kord = getKoin().get<Kord>()

    kord.on<MessageCreateEvent> {
        // ignore other bots, even ourselves. We only serve humans here!
        if (message.author?.isBot != false) return@on
        try {
            mapMessageHandler.handle(this)
//            messageHandler.handleMessage(this)
        } catch (ex: UnknownCommandException) {
            message.channel.createMessage(
                "They didn't teach that at PolyTech :<"
            )
            return@on
        } catch (ex: Exception) {
            logger.error(ex) { ex.message }
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


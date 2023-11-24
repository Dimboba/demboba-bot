package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.controller.MessageHandler
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.ksp.generated.module
import org.ktorm.database.Database

var kord: Kord? = null

private val logger = KotlinLogging.logger { }
suspend fun main(args: Array<String>) {
    val dbPort = "localhost:5432"
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/dembobabot",
        user = "postgres",
        password = "qwe",
        driver = "org.postgresql.Driver"
    )

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
    //TODO: create MessageQueue with coroutine start of working with messages
    val messageHandler = getKoin().get<MessageHandler>()
    kord!!.on<MessageCreateEvent> {

        // ignore other bots, even ourselves. We only serve humans here!
        if (message.author?.isBot != false) return@on
        try {
            messageHandler.handleMessage(this)
        } catch (ex: UnknownCommandException) {
            message.channel.createMessage(
                "В Политехе такому не учили :<"
            )
            return@on
        } catch (ex: Exception) {
            logger.error {
                ex.message
            }
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


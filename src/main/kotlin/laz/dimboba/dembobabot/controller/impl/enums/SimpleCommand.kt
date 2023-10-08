package laz.dimboba.dembobabot.controller.impl.enums

import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import laz.dimboba.dembobabot.exceptions.NotEnoughArgumentsException
import laz.dimboba.dembobabot.overwatch.OverbuffReader
import laz.dimboba.dembobabot.overwatch.OverwatchPlayerStats
import java.text.SimpleDateFormat
import java.util.*

enum class SimpleCommand {
    PING {
        override suspend fun exec(args: List<String>, message: Message) {
            message.channel.createMessage("pong!")
        }
    },
    TIME {
        override suspend fun exec(args: List<String>, message: Message) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val currDate = Calendar.getInstance().time
            message.channel.createMessage(
                sdf.format(currDate)
            )
        }
    },
    HELLO {
        override suspend fun exec(args: List<String>, message: Message) =
            sayHello(args, message)
    },
    HI {
        override suspend fun exec(args: List<String>, message: Message) =
            sayHello(args, message)
    },
    FUCK_YOU {
        override suspend fun exec(args: List<String>, message: Message) =
            sayFU(args, message)
    },
    FUCKYOU {
        override suspend fun exec(args: List<String>, message: Message) =
            sayFU(args, message)
    },
    FU {
        override suspend fun exec(args: List<String>, message: Message) =
            sayFU(args, message)
    },
    FUCK {
        override suspend fun exec(args: List<String>, message: Message) =
            sayFU(args, message)
    },
    OVERWATCH {
        override suspend fun exec(args: List<String>, message: Message) {
            message.channel.createMessage(
                getAllStats(args)
            )
        }
    };

    protected suspend fun sayFU(args: List<String>, message: Message) {
        message.reply {
            content = "Fuck off, ${message.author?.username ?: "dude"}"
        }
    }

    protected suspend fun sayHello(args: List<String>, message: Message) {
        message.reply {
            content = "Hi, ${message.author?.username ?: "dude"} (^◡^)/"
        }
    }

    protected fun getAllStats(text: List<String>): String {
        val stats: List<OverwatchPlayerStats>

        try {
            stats = OverbuffReader().getSeparatePlayerStats(text[1])
        } catch (ex: IndexOutOfBoundsException) {
            throw NotEnoughArgumentsException("Not enough arguments")
        }

        return """
        Player: ${stats[0].name}
        ${stats[0].gameMode}: 
        ${stats[0].toBeautifulString()}
        ${stats[1].gameMode}: 
        ${stats[1].toBeautifulString()}
        ${stats[2].gameMode}: 
        ${stats[2].toBeautifulString()}
    """.trimIndent()
    }

    abstract suspend fun exec(args: List<String>, message: Message)
}
package laz.dimboba.dembobabot.controller

import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import laz.dimboba.dembobabot.exceptions.NotEnoughArgumentsException
import laz.dimboba.dembobabot.overwatch.OverbuffReader
import laz.dimboba.dembobabot.overwatch.OverwatchPlayerStats
import java.text.SimpleDateFormat
import java.util.*


@CommandAction("ping")
suspend fun pong(args: List<String>, message: Message) {
    message.channel.createMessage("pong!")
}

@CommandAction("time", "currenttime", "current-time")
suspend fun time(args: List<String>, message: Message) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val currDate = Calendar.getInstance().time
    message.channel.createMessage(
        "Current time is ${sdf.format(currDate)}"
    )
}

@CommandAction("hi", "hello")
suspend fun sayHello(args: List<String>, message: Message) {
    message.reply {
        content = "Hi, ${message.author?.username ?: "dude"} (^◡^)/"
    }
}

@CommandAction("fuckyou", "fuck-you", "fu", "fuck")
suspend fun sayFU(args: List<String>, message: Message) {
    message.reply {
        content = "Fuck off, ${message.author?.username ?: "dude"}"
    }
}

@CommandAction("overwatch", "overbuff")
suspend fun getAllStats(text: List<String>, message: Message) {
    val stats: List<OverwatchPlayerStats>

    try {
        //todo: maybe it is bad to create every run, but should not be a problem
        stats = OverbuffReader().getSeparatePlayerStats(text[1])
    } catch (ex: IndexOutOfBoundsException) {
        throw NotEnoughArgumentsException("Not enough arguments")
    }

    message.reply {
        content = """
    Player: ${stats[0].name}
    ${stats[0].gameMode}: 
    ${stats[0].toBeautifulString()}
    ${stats[1].gameMode}: 
    ${stats[1].toBeautifulString()}
    ${stats[2].gameMode}: 
    ${stats[2].toBeautifulString()}
""".trimIndent()
    }
}
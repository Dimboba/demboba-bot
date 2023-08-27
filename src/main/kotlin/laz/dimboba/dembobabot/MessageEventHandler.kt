package laz.dimboba.dembobabot

import dev.kord.core.entity.Message
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.NotEnoughArgumentsException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import java.text.SimpleDateFormat
import java.util.*

const val commandChar: Char = '!'

fun handleMessage(message: Message): String {

    val text = message.content.split(" ")

    val keyword: String;
    try {
        keyword = text[0];
    } catch(ex: IndexOutOfBoundsException) {
        throw NotACommandMessageException("Message: there is no commands")
    }

    if(keyword.length < 2 || keyword[0] != commandChar )
        throw NotACommandMessageException("Message: \"$text\" is not a command")

    //TODO: вероятно, лучше по ключу в мапке передавать функцию что делать

    return when (val command = keyword.substring(1, keyword.length).lowercase(Locale.getDefault())) {
        "ping" -> "pong!"
        "time" -> getTime()
        "hello", "hi" -> "Hi, ${getName(message)} (^◡^)/"
        "fuckyou", "fuck", "fu", "fyou", "fucku" -> "Fuck off, ${getName(message)}"
        "overwatch" -> getAllStats(text)

        else -> throw UnknownCommandException("Unknown command: \"$command\"")
    }
}

fun getName(message: Message): String {
    return message.author?.username ?: "dude"
}

fun getTime(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val currDate = Calendar.getInstance().time
    return sdf.format(currDate)
}

//parsed string here
fun getAllStats(text: List<String>): String {
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
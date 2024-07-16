package laz.dimboba.dembobabot.help

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.controller.CommandAction
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.lang.reflect.Method
import java.util.*

private val logger = KotlinLogging.logger {}

@Singleton
class HelpController: KoinComponent {

    private val descriptions: Properties = Properties()
    private val usages: Properties = Properties()
    private val annotatedMethods: List<Method> by inject(named("commandMethods"))
    private val commandToMethodName: Map<String, String>
    private val methodNameToCommands: Map<String, Array<String>>

    init {
        val annotation = CommandAction::class.java
        val annotations = annotatedMethods
            .map { it.getAnnotation(annotation) }
        commandToMethodName = annotations
            .flatMap { commandAnnotation ->
                commandAnnotation.commands
                    .map { it to commandAnnotation.name }
            }
            .toMap()
        methodNameToCommands = annotations.associate { it.name to it.commands }
    }

    init {
        val descriptionFile = this::class.java.classLoader.getResourceAsStream("commands/description.properties")
        val usageFile = this::class.java.classLoader.getResourceAsStream("commands/usage.properties")

        descriptions.load(descriptionFile)
        usages.load(usageFile)

        logger.info { "HelpController is started" }
    }

    @CommandAction("list", ["list"])
    suspend fun listCommands(messageCreateEvent: MessageCreateEvent) {
        messageCreateEvent.message.reply {
            content = getCommandList()
        }
    }

    @CommandAction("help", ["help"])
    suspend fun help(messageCreateEvent: MessageCreateEvent, args: List<String>) {
        messageCreateEvent.message.reply {
            content = getHelpReplyMessage(args)
        }
    }

    private fun getCommandList(): String {
        return methodNameToCommands.map { (methodName, commands) ->
            val description = descriptions.getProperty("command.description.$methodName") ?: ""
            val commandString = commands.joinToString(separator = ", ")
            return@map "$commandString - $description"
        }.joinToString("\n")
    }

    private fun getHelpReplyMessage(args: List<String>): String {
        if(args.size < 2 || args[1] == "") {
            return descriptions.getProperty("command.description.help")
        }
        val command = commandToMethodName[args[1]]
            ?: return "Command does not exist\n${descriptions.getProperty("command.description.help")}"

        val description = descriptions.getProperty("command.description.${command}")
        val usage = usages.getProperty("command.usage.${command}") ?: ""

        return "${description}\n${usage}"
    }
}
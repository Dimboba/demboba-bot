package laz.dimboba.dembobabot.controller

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownClassForCommandException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.help.HelpController
import laz.dimboba.dembobabot.voice.TrackScheduler
import laz.dimboba.dembobabot.voice.TrackSchedulerProvider
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger { }
@Singleton
class MapMessageHandler : KoinComponent {

    private val commandMap: Map<String, Method>
    private val schedulerProvider: TrackSchedulerProvider by inject()
    private val helpController: HelpController by inject()
    private val annotatedMethods: List<Method> by inject(named("commandMethods"))

    private fun collectCommands(): Map<String, Method> {
        val annotation = CommandAction::class.java
        return annotatedMethods
            .flatMap {
                it.getAnnotation(annotation)
                    .commands
                    .map { command -> command.lowercase() to it } // don't know about lowercase
            }
            .toMap()
    }

    init {
        commandMap = collectCommands()
        logger.info { "MapMessageHandler with ${commandMap.size} commands is started" }
    }

    private var commandChar = '!'

    suspend fun handle(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        val args = parseCommand(message.content)

        val method = commandMap[args[0]]
            ?: throw UnknownCommandException("Unknown command ${messageCreateEvent.message.content}")

        if(Modifier.isStatic(method.modifiers)) {
            method.invokeCommand(null, messageCreateEvent, args)
            return
        }
        //todo : add several servers handling
        // now it is easy to determine scheduler/handler by server id
        when(method.declaringClass) {
            TrackScheduler::class.java -> method.invokeCommand(
                schedulerProvider.getScheduler(messageCreateEvent),
                messageCreateEvent,
                args
            )
            HelpController::class.java -> method.invokeCommand(helpController, messageCreateEvent, args)
            else -> throw UnknownClassForCommandException(
                "There is no handler for class ${method.declaringClass.canonicalName} " +
                "required for command ${args[0]}")
        }
    }

    private suspend fun Method.invokeCommand(invoker: Any?, event: MessageCreateEvent, args: List<String>) {
        val argumentsMap = mapOf<Class<*>, Any>(
            MessageCreateEvent::class.java to event,
            List::class.java to args,
            Message::class.java to event.message
        )

        val methodArguments = this.parameterTypes
            .map { type -> argumentsMap[type] }


        // if function is suspend it has invisible continuation parameter at the end
        if (methodArguments.last() is Continuation<*>?) {
            invokeSuspend(invoker, *(methodArguments.dropLast(1).toTypedArray()))
            return
        }
        invoke(invoker, *methodArguments.toTypedArray())
    }

    private suspend fun Method.invokeSuspend(obj: Any?, vararg args: Any?): Any? =
        suspendCoroutine { cont ->
            invoke(obj, *args, cont)
        }

    private fun parseCommand(messageContent: String): List<String> {
        val content = messageContent.split(" ").toMutableList()
        if (content.isEmpty())
            throw NotACommandMessageException("There is no commands")

        content[0] = content[0].lowercase()

        if (content[0].length < 2 || content[0][0] != commandChar)
            throw NotACommandMessageException("Message: \"${content[0]}\" is not a command")
        content[0] = content[0].removeRange(0, 1)
        return content
    }
}
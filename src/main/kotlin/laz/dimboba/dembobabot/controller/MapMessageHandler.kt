package laz.dimboba.dembobabot.controller

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import io.github.classgraph.ClassGraph
import laz.dimboba.dembobabot.channel.ChannelController
import laz.dimboba.dembobabot.exceptions.NotACommandMessageException
import laz.dimboba.dembobabot.exceptions.UnknownCommandException
import laz.dimboba.dembobabot.voice.TrackScheduler
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger { }
@Singleton
class MapMessageHandler : KoinComponent {

    //todo: collect to map through annotation
    private val commandMap: Map<String, Method>
    private val trackScheduler: TrackScheduler by inject()
    private val channelController: ChannelController by inject()

    private fun collectCommands(): Map<String, Method> {
        val annotation = CommandAction::class.java
        return ClassGraph()
            .ignoreClassVisibility()
            .enableAllInfo()
            .acceptPackages("laz.dimboba.dembobabot")
            .scan(). use { scanResult ->
                scanResult.allClasses
                    .flatMap { classInfo ->
                        classInfo.loadClass()
                            .declaredMethods
                            .filter { it.getAnnotation(annotation) != null}
                    }
                    .flatMap {
                        it.getAnnotation(annotation)
                        .commands
                        .map { command -> command.lowercase() to it }
                    }
                    .toMap()
            }
    }

    init {
        logger.info("MapMessageHandler is started")
        commandMap = collectCommands()
    }

    private var commandChar = '!'

    suspend fun handle(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        val args = parseCommand(message.content)

        val method = commandMap[args[0]]
            ?: throw UnknownCommandException("Unknown command ${messageCreateEvent.message.content}")

        if(Modifier.isStatic(method.modifiers)) {
            method.invokeCommand(null, messageCreateEvent, args)
        }
        //todo : add several servers handling
        // now it is easy to determine scheduler/handler by server id
        when(method.declaringClass) {
            TrackScheduler::class.java -> method.invokeCommand(trackScheduler, messageCreateEvent, args)
            ChannelController::class.java -> method.invokeCommand(channelController, messageCreateEvent, args)
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


        // if function is suspend it has invisible continuation parameter at end
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
            throw NotACommandMessageException("Message: there is no commands")

        content[0] = content[0].lowercase()

        if (content[0].length < 2 || content[0][0] != commandChar)
            throw NotACommandMessageException("Message: \"${content[0]}\" is not a command")
        content[0] = content[0].removeRange(0, 1)
        return content
    }
}
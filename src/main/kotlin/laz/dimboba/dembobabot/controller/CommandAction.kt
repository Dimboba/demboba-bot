package laz.dimboba.dembobabot.controller

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandAction(
    val name: String,
    val commands: Array<String>
)

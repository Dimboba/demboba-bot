package laz.dimboba.dembobabot.controller

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandAction(vararg val commands: String)

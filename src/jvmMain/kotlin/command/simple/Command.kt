package org.chorus_oss.chorus.command.simple

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class Command(
    val name: String,
    val description: String = "",
    val usageMessage: String = "",
    val aliases: Array<String> = []
)
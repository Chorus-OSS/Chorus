package org.chorus_oss.chorus.command.simple

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class CommandParameters(val parameters: Array<Parameters> = [])

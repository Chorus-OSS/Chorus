package org.chorus_oss.chorus.command.simple

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class Parameters(val name: String, val parameters: Array<Parameter>)

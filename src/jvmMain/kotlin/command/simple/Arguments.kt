package org.chorus_oss.chorus.command.simple

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class Arguments(val min: Int = 0, val max: Int = 0)
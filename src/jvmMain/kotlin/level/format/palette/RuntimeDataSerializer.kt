package org.chorus_oss.chorus.level.format.palette

fun interface RuntimeDataSerializer<V> {
    fun serialize(value: V): Int
}

package org.chorus_oss.chorus.level.format.palette

fun interface RuntimeDataDeserializer<V> {
    fun deserialize(id: Int): V
}

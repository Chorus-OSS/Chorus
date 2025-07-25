package org.chorus_oss.chorus.nbt.tag

import java.util.*

abstract class NumberTag<T : Number> : Tag<T>() {
    abstract var data: T

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as NumberTag<*>

        return data == other.data
    }
}

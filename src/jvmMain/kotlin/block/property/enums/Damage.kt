package org.chorus_oss.chorus.block.property.enums

enum class Damage {
    UNDAMAGED,
    SLIGHTLY_DAMAGED,
    VERY_DAMAGED,
    BROKEN;

    fun next(): Damage {
        return entries[ordinal + 1]
    }
}

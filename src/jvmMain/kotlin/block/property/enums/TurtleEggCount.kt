package org.chorus_oss.chorus.block.property.enums

enum class TurtleEggCount {
    ONE_EGG,
    TWO_EGG,
    THREE_EGG,
    FOUR_EGG;

    fun before(): TurtleEggCount {
        return entries[ordinal - 1]
    }

    fun next(): TurtleEggCount {
        return entries[ordinal + 1]
    }
}

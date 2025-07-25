package org.chorus_oss.chorus.block.property.enums

enum class CrackedState {
    NO_CRACKS,
    CRACKED,
    MAX_CRACKED;

    fun next(): CrackedState {
        return entries[ordinal + 1]
    }
}

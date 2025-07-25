package org.chorus_oss.chorus.block.property.enums

enum class StructureBlockType {
    INVALID,
    DATA,
    SAVE,
    LOAD,
    CORNER,
    EXPORT;

    companion object {
        fun from(id: Int): StructureBlockType {
            return entries[id]
        }
    }
}

package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockUnpoweredComparator @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRedstoneComparator(blockstate) {
    override val name: String
        get() = "Comparator Block Unpowered"

    override val unpowered: BlockRedstoneComparator
        get() = this

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.UNPOWERED_COMPARATOR,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OUTPUT_LIT_BIT,
            CommonBlockProperties.OUTPUT_SUBTRACT_BIT
        )
    }
}
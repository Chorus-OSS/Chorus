package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockSculkVein @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLichen(blockstate) {
    override val name: String
        get() = "Sculk Vein"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SCULK_VEIN, CommonBlockProperties.MULTI_FACE_DIRECTION_BITS)
    }
}

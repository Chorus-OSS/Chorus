package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPaleOakStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStairsWood(blockstate) {
    override val name: String
        get() = "Pale Oak Wood Stairs"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.PALE_OAK_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}
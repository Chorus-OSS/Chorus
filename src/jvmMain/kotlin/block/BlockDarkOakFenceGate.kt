package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDarkOakFenceGate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFenceGate(blockstate) {
    override val name: String
        get() = "Dark Oak Fence Gate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.DARK_OAK_FENCE_GATE,
            CommonBlockProperties.IN_WALL_BIT,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT
        )
    }
}
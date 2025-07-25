package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPaleOakFenceGate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFenceGate(blockstate) {
    override val name: String
        get() = "Pale Oak Fence Gate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.PALE_OAK_FENCE_GATE,
            CommonBlockProperties.IN_WALL_BIT,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT
        )

    }
}
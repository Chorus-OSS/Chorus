package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockAzaleaLeavesFlowered @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockAzaleaLeaves(blockState) {
    override val name: String
        get() = "Azalea Leaves Flowered"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.AZALEA_LEAVES_FLOWERED,
            CommonBlockProperties.PERSISTENT_BIT,
            CommonBlockProperties.UPDATE_BIT
        )
    }
}

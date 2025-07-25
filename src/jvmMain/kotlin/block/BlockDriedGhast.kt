package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDriedGhast(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    override val waterloggingLevel: Int
        get() = 1

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.DRIED_GHAST, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.REHYDRATION_LEVEL
        )
    }
}
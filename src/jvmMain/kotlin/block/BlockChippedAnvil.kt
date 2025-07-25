package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockChippedAnvil @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockAnvil(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHIPPED_ANVIL, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}

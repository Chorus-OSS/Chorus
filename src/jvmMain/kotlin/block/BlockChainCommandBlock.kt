package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockChainCommandBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCommandBlock(blockstate) {
    override val name: String
        get() = "Chain Command Block"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CHAIN_COMMAND_BLOCK,
            CommonBlockProperties.CONDITIONAL_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )
    }
}
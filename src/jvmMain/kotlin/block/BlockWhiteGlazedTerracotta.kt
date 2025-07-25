package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWhiteGlazedTerracotta @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlazedTerracotta(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WHITE_GLAZED_TERRACOTTA, CommonBlockProperties.FACING_DIRECTION)
    }
}
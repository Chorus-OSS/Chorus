package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockGrayGlazedTerracotta @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlazedTerracotta(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.GRAY_GLAZED_TERRACOTTA, CommonBlockProperties.FACING_DIRECTION)
    }
}
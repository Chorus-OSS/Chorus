package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockChiseledQuartzBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockQuartzBlock(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHISELED_QUARTZ_BLOCK, CommonBlockProperties.PILLAR_AXIS)
    }
}

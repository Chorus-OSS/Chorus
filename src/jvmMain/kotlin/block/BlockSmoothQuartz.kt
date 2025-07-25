package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockSmoothQuartz @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSandstone(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SMOOTH_QUARTZ, CommonBlockProperties.PILLAR_AXIS)
    }
}

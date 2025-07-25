package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockPolishedBasalt @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBasalt(blockstate) {
    override val name: String
        get() = "Polished Basalt"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POLISHED_BASALT, CommonBlockProperties.PILLAR_AXIS)

    }
}

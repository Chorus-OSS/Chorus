package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBambooBlock @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockLog(blockState) {
    override val name: String
        get() = "Bamboo Block"

    override val resistance: Double
        get() = 15.0

    override val burnAbility: Int
        get() = 20

    override fun getStrippedState(): BlockState {
        return BlockStrippedBambooBlock.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BAMBOO_BLOCK, CommonBlockProperties.PILLAR_AXIS)
    }
}
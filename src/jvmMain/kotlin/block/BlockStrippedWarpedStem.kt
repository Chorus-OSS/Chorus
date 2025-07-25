package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockStrippedWarpedStem @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStemStripped(blockstate) {
    override val name: String
        get() = "Stripped Warped Stem"

    override val burnChance: Int
        get() = 0

    override val burnAbility: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_WARPED_STEM, CommonBlockProperties.PILLAR_AXIS)
    }
}
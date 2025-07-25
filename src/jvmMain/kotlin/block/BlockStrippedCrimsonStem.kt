package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockStrippedCrimsonStem @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStemStripped(blockstate) {
    override val name: String
        get() = "Stripped Crimson Stem"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_CRIMSON_STEM, CommonBlockProperties.PILLAR_AXIS)
    }
}
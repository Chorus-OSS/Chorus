package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockVerdantFroglight @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFroglight(blockState) {
    override val name: String
        get() = "Verdant Froglight"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.VERDANT_FROGLIGHT,
            CommonBlockProperties.PILLAR_AXIS
        )
    }
}

package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockJungleTrapdoor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTrapdoor(blockstate) {
    override val name: String
        get() = "Jungle Trapdoor"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.JUNGLE_TRAPDOOR,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPSIDE_DOWN_BIT
        )
    }
}
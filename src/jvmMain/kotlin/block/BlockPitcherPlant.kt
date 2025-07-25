package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType

//todo complete
class BlockPitcherPlant @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoublePlant(blockstate) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.PITCHER_PLANT

    override val name: String
        get() = "Pitcher Plant"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PITCHER_PLANT, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}
package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockWeatheredCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperBlock(blockstate) {
    override val name: String
        get() = "Weathered Copper"

    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.WEATHERED
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WEATHERED_COPPER)

    }
}
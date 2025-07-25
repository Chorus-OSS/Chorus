package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockWeatheredCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCutCopper(blockstate) {
    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.WEATHERED
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WEATHERED_CUT_COPPER)
    }
}
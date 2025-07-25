package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockWeatheredChiseledCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockChiseledCopperBase(blockstate) {
    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.WEATHERED
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WEATHERED_CHISELED_COPPER)
    }
}
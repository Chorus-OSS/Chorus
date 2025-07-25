package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockWaxedExposedChiseledCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockChiseledCopperBase(blockstate) {
    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.EXPOSED
        }

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_EXPOSED_CHISELED_COPPER)
    }
}
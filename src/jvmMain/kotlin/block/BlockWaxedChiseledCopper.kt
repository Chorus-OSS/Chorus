package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockWaxedChiseledCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockChiseledCopperBase(blockstate) {
    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED

    override val isWaxed
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_CHISELED_COPPER)
    }
}
package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockExposedChiseledCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockChiseledCopperBase(blockstate) {
    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.EXPOSED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.EXPOSED_CHISELED_COPPER)
    }
}
package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockCopperGrate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperGrateBase(blockstate) {

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.COPPER_GRATE)
    }
}
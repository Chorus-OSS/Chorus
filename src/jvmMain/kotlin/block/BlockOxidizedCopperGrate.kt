package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockOxidizedCopperGrate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperGrateBase(blockstate) {
    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.OXIDIZED
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.OXIDIZED_COPPER_GRATE)
    }
}
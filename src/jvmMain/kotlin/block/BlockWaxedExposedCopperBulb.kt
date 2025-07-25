package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

class BlockWaxedExposedCopperBulb @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperBulbBase(blockstate) {
    override val oxidizationLevel
        get(): OxidizationLevel {
            return OxidizationLevel.EXPOSED
        }

    override val isWaxed: Boolean
        get() = true

    override val lightLevel: Int
        get() = if (lit) 12 else 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WAXED_EXPOSED_COPPER_BULB,
            CommonBlockProperties.LIT,
            CommonBlockProperties.POWERED_BIT
        )
    }
}
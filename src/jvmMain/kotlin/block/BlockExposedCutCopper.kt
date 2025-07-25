package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockExposedCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCutCopper(blockstate) {
    override val name: String
        get() = "Exposed Cut Copper"

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.EXPOSED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.EXPOSED_CUT_COPPER)
    }
}
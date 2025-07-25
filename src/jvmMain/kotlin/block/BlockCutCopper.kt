package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperBase(blockstate) {
    override val name: String
        get() = "Cut Copper"

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED


    override fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String {
        if (oxidizationLevel == null) {
            return id
        }
        return when (oxidizationLevel) {
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_CUT_COPPER else BlockID.CUT_COPPER
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_CUT_COPPER else BlockID.EXPOSED_CUT_COPPER
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_CUT_COPPER else BlockID.WEATHERED_CUT_COPPER
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_CUT_COPPER else BlockID.OXIDIZED_CUT_COPPER
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CUT_COPPER)
    }
}
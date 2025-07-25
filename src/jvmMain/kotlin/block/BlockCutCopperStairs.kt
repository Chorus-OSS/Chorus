package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockCutCopperStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStairsCopperBase(blockstate) {
    override val name: String
        get() {
            val sb = StringBuilder(30)
            if (isWaxed) {
                sb.append("Waxed ")
            }
            val oxidizationLevel: OxidizationLevel = oxidizationLevel
            if (OxidizationLevel.UNAFFECTED != oxidizationLevel) {
                val name: String = oxidizationLevel.name
                sb.append(name[0]).append(name.substring(1).lowercase()).append(' ')
            }
            return sb.append("Cut Copper Stairs").toString()
        }

    override fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String {
        if (oxidizationLevel == null) {
            return id
        }
        return when (oxidizationLevel) {
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_CUT_COPPER_STAIRS else BlockID.CUT_COPPER_STAIRS
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_CUT_COPPER_STAIRS else BlockID.EXPOSED_CUT_COPPER_STAIRS
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_CUT_COPPER_STAIRS else BlockID.WEATHERED_CUT_COPPER_STAIRS
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_CUT_COPPER_STAIRS else BlockID.OXIDIZED_CUT_COPPER_STAIRS
        }
    }

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CUT_COPPER_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}
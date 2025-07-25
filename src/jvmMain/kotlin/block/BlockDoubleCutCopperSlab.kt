package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockDoubleCutCopperSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleSlabCopperBase(blockstate) {
    override fun getSlabName(): String {
        val sb = StringBuilder(30)
        if (isWaxed) {
            sb.append("Waxed ")
        }
        val oxidizationLevel = oxidizationLevel
        if (OxidizationLevel.UNAFFECTED != oxidizationLevel) {
            val name = oxidizationLevel.name
            sb.append(name[0]).append(name.substring(1).lowercase()).append(' ')
        }
        return sb.append("Cut Copper").toString()
    }

    override fun getSingleSlab() = BlockCutCopperSlab.properties.defaultState

    override fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String {
        if (oxidizationLevel == null) {
            return id
        }
        return when (oxidizationLevel) {
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_DOUBLE_CUT_COPPER_SLAB else BlockID.DOUBLE_CUT_COPPER_SLAB
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB else BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB else BlockID.WEATHERED_DOUBLE_CUT_COPPER_SLAB
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB else BlockID.OXIDIZED_DOUBLE_CUT_COPPER_SLAB
        }
    }

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DOUBLE_CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
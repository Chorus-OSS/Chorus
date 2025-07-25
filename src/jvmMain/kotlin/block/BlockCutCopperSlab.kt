package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockCutCopperSlab @JvmOverloads constructor(
    blockState: BlockState = properties.defaultState,
    doubleSlabId: String
) : BlockSlabCopperBase(blockState, doubleSlabId) {

    override fun getSlabName(): String {
        val sb = StringBuilder(30)
        if (isWaxed) {
            sb.append("Waxed ")
        }
        val oxidizationLevel: OxidizationLevel = oxidizationLevel
        if (OxidizationLevel.UNAFFECTED != oxidizationLevel) {
            val name: String = oxidizationLevel.name
            sb.append(name[0]).append(name.substring(1).lowercase()).append(' ')
        }
        return sb.append("Cut Copper").toString()
    }

    override fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String {
        if (oxidizationLevel == null) {
            return id
        }
        return when (oxidizationLevel) {
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_CUT_COPPER_SLAB else BlockID.CUT_COPPER_SLAB
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB else BlockID.EXPOSED_CUT_COPPER_SLAB
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_CUT_COPPER_SLAB else BlockID.WEATHERED_CUT_COPPER_SLAB
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_CUT_COPPER_SLAB else BlockID.OXIDIZED_CUT_COPPER_SLAB
        }
    }

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.UNAFFECTED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
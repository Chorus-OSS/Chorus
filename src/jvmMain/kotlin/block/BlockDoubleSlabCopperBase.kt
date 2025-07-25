package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

abstract class BlockDoubleSlabCopperBase(blockstate: BlockState) : BlockDoubleSlabBase(blockstate),
    Waxable, Oxidizable {
    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 6.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        return super<Waxable>.onActivate(item, player, blockFace, fx, fy, fz)
                || super<Oxidizable>.onActivate(item, player, blockFace, fx, fy, fz)
    }

    override fun onUpdate(type: Int): Int {
        return super<Oxidizable>.onUpdate(type)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun getBlockWithOxidizationLevel(oxidizationLevel: OxidizationLevel): Block {
        return get(getCopperId(isWaxed, oxidizationLevel)).setPropertyValues(propertyValues)
    }

    override fun setOxidizationLevel(oxidizationLevel: OxidizationLevel): Boolean {
        if (oxidizationLevel == oxidizationLevel) {
            return true
        }
        return level.setBlock(
            this.position,
            get(getCopperId(isWaxed, oxidizationLevel)).setPropertyValues(propertyValues)
        )
    }

    override fun setWaxed(waxed: Boolean): Boolean {
        if (isWaxed == waxed) {
            return true
        }
        return level.setBlock(
            this.position, get(
                getCopperId(
                    isWaxed,
                    oxidizationLevel
                )
            ).setPropertyValues(propertyValues)
        )
    }

    override val isWaxed: Boolean
        get() = false


    protected abstract fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String
}

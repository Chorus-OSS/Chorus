package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSnowball
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockSnow(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Snow"

    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 1.0

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isShovel && item.tier >= ItemTool.TIER_WOODEN) {
            arrayOf(ItemSnowball(0, 4))
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isShovel) {
            item.useOn(this)
            level.useBreakOn(this.position, item.clone().clearNamedTag(), null, true)
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SNOW)
    }
}

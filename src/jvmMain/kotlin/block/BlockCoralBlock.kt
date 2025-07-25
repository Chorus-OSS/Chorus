package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

abstract class BlockCoralBlock(blockstate: BlockState) : BlockSolid(blockstate) {
    open val isDead: Boolean
        get() = false

    open fun toDead(): BlockCoralBlock {
        return this
    }

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 6.0

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isDead) {
                level.scheduleUpdate(this, 60 + ThreadLocalRandom.current().nextInt(40))
            }
            return type
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!isDead) {
                for (face in BlockFace.entries) {
                    if (getSideAtLayer(0, face) is BlockFlowingWater || getSideAtLayer(1, face) is BlockFlowingWater
                        || getSideAtLayer(0, face) is BlockFrostedIce || getSideAtLayer(1, face) is BlockFrostedIce
                    ) {
                        return type
                    }
                }
                val event = BlockFadeEvent(this, toDead())
                if (!event.cancelled) {
                    level.setBlock(this.position, event.newState, direct = true, update = true)
                }
            }
            return type
        }
        return 0
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
            if (item.getEnchantment(Enchantment.ID_SILK_TOUCH) != null) {
                arrayOf(toItem())
            } else {
                arrayOf(toDead().toItem())
            }
        } else {
            Item.EMPTY_ARRAY
        }
    }
}

package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level


open class BlockIce(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Ice"

    override val resistance: Double
        get() = 2.5

    override val hardness: Double
        get() = 0.5

    override val frictionFactor: Double
        get() = 0.98

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun onBreak(item: Item?): Boolean {
        if (item != null) {
            if (level.dimension == Level.DIMENSION_NETHER || item.getEnchantmentLevel(Enchantment.ID_SILK_TOUCH) > 0 || down().isAir) {
                return super.onBreak(item)
            }
        }

        return level.setBlock(this.position, get(BlockID.FLOWING_WATER), true)
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (level.getBlockLightAt(
                    position.x.toInt(),
                    position.y.toInt(), position.z.toInt()
                ) >= 12
            ) {
                val event = BlockFadeEvent(
                    this,
                    if (level.dimension == Level.DIMENSION_NETHER) get(BlockID.AIR) else get(BlockID.FLOWING_WATER)
                )
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    level.setBlock(this.position, event.newState, true)
                }
                return Level.BLOCK_UPDATE_RANDOM
            }
        }
        return 0
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val burnChance: Int
        get() = -1

    override val lightFilter: Int
        get() = 2

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.ICE)
    }
}

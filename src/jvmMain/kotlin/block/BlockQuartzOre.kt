package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemQuartz
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom

class BlockQuartzOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Quartz Ore"

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 5.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun getDrops(item: Item): Array<Item> {
        if (item.isPickaxe && item.tier >= toolTier) {
            var count = 1
            val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
            if (fortune != null && fortune.level >= 1) {
                var i = ThreadLocalRandom.current().nextInt(fortune.level + 2) - 1

                if (i < 0) {
                    i = 0
                }

                count = i + 1
            }

            return arrayOf(
                ItemQuartz(0, count)
            )
        } else {
            return Item.EMPTY_ARRAY
        }
    }

    override val dropExp: Int
        get() = ChorusRandom().nextInt(1, 5)

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.QUARTZ_ORE)
    }
}
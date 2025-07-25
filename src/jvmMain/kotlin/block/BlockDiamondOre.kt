package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDiamond
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom

open class BlockDiamondOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOre(blockstate) {

    override val rawMaterial: String?
        get() = ItemID.DIAMOND

    override val name: String
        get() = "Diamond Ore"

    override val toolTier: Int
        get() = ItemTool.TIER_IRON

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
                ItemDiamond(0, count)
            )
        } else {
            return Item.EMPTY_ARRAY
        }
    }

    override val dropExp: Int
        get() = ThreadLocalRandom.current().nextInt(3, 8)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DIAMOND_ORE)
    }
}
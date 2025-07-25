package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom

open class BlockLapisOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOre(blockstate) {
    override val name: String
        get() = "Lapis Ore"

    override fun getDrops(item: Item): Array<Item> {
        if (item.isPickaxe && item.tier >= toolTier) {
            val random = ThreadLocalRandom.current()
            var count = 4 + random.nextInt(5)
            val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
            if (fortune != null && fortune.level >= 1) {
                var i = random.nextInt(fortune.level + 2) - 1

                if (i < 0) {
                    i = 0
                }

                count *= (i + 1)
            }
            val itemRaw = Item.get(ItemID.LAPIS_LAZULI)
            itemRaw.setCount(count)
            return arrayOf(itemRaw)
        } else {
            return Item.EMPTY_ARRAY
        }
    }

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override val rawMaterial: String?
        get() = ItemID.LAPIS_LAZULI

    override val dropExp: Int
        get() = ThreadLocalRandom.current().nextInt(2, 6)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LAPIS_ORE)
    }
}
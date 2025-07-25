package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.utils.ChorusRandom

class BlockNetherGoldOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGoldOre(blockstate) {
    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val name: String
        get() = "Nether Gold Ore"

    override fun getDrops(item: Item): Array<Item> {
        if (!item.isPickaxe || item.tier < ItemTool.TIER_WOODEN) {
            return Item.EMPTY_ARRAY
        }

        val enchantment = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
        var fortune = 0
        if (enchantment != null) {
            fortune = enchantment.level
        }

        val random = ChorusRandom()
        var count: Int = random.nextInt(2, 6)
        when (fortune) {
            0 -> {
                // Does nothing
            }

            1 -> {
                if (random.nextInt(0, 2) == 0) {
                    count *= 2
                }
            }

            2 -> {
                if (random.nextInt(0, 1) == 0) {
                    count *= random.nextInt(2, 3)
                }
            }

            else -> {
                if (random.nextInt(0, 4) < 3) {
                    count *= random.nextInt(2, 4)
                }
            }
        }

        return arrayOf(get(ItemID.GOLD_NUGGET, 0, count))
    }

    override val rawMaterial: String
        get() = ItemID.GOLD_NUGGET

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHER_GOLD_ORE)

    }
}
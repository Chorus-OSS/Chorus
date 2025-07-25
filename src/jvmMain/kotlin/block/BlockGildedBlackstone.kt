package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom

class BlockGildedBlackstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Gilded Blackstone"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun getDrops(item: Item): Array<Item> {
        if (!item.isPickaxe || item.tier < ItemTool.TIER_WOODEN) {
            return Item.EMPTY_ARRAY
        }

        val dropOdds: Int
        var fortune = 0
        val enchantment = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
        if (enchantment != null) {
            fortune = enchantment.level
        }

        dropOdds = when (fortune) {
            0 -> 10
            1 -> 7
            2 -> 4
            else -> 1
        }

        val random = ThreadLocalRandom.current()
        if (dropOdds > 1 && random.nextInt(dropOdds) != 0) {
            return arrayOf(toItem())
        }

        return arrayOf(get(ItemID.GOLD_NUGGET, 0, random.nextInt(2, 6)))
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 6.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GILDED_BLACKSTONE)
    }
}
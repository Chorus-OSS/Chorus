package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom

abstract class BlockOre(blockState: BlockState) : BlockSolid(blockState) {
    override fun getDrops(item: Item): Array<Item> {
        if (!canHarvest(item)) {
            return Item.EMPTY_ARRAY
        }

        val rawMaterial = rawMaterial ?: return super.getDrops(item)

        val multiplier = dropMultiplier
        var amount = multiplier.toInt()
        if (amount > 1) {
            amount = 1 + ThreadLocalRandom.current().nextInt(amount)
        }

        val fortuneLevel = item.getEnchantmentLevel(Enchantment.ID_FORTUNE_DIGGING).coerceIn(0, 3)
        if (fortuneLevel > 0) {
            val increase = ThreadLocalRandom.current().nextInt((multiplier * fortuneLevel).toInt() + 1)
            amount += increase
        }

        val itemRaw = Item.get(rawMaterial)
        itemRaw.setCount(amount)
        return arrayOf(itemRaw)
    }

    protected abstract val rawMaterial: String?

    protected open val dropMultiplier: Float
        get() = 1f

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 3.0
}

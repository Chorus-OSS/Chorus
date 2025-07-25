package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemBeetrootSeeds
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class BlockBeetroot @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCrops(blockstate) {
    override val name: String
        get() = "Beetroot Block"

    override fun toItem(): Item {
        return ItemBeetrootSeeds()
    }

    override fun getDrops(item: Item): Array<Item> {
        if (!isFullyGrown) {
            return arrayOf(Item.get(ItemID.BEETROOT_SEEDS))
        }

        var seeds = 1
        val attempts = (3 + min(
            0.0,
            item.getEnchantmentLevel(Enchantment.ID_FORTUNE_DIGGING).toDouble()
        )).toInt()
        val random = ThreadLocalRandom.current()
        for (i in 0..<attempts) {
            if (random.nextInt(7) < 4) { // 4/7, 0.57142857142857142857142857142857
                seeds++
            }
        }

        return arrayOf(
            Item.get(BlockID.BEETROOT),
            get(ItemID.BEETROOT_SEEDS, 0, seeds)
        )
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BEETROOT, CommonBlockProperties.GROWTH)
    }
}

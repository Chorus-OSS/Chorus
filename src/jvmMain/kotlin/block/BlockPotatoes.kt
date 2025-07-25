package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class BlockPotatoes @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCrops(blockstate) {
    override val name: String
        get() = "Potato Block"

    override fun toItem(): Item {
        return Item.get(ItemID.POTATO)
    }

    override fun getDrops(item: Item): Array<Item> {
        if (!isFullyGrown) {
            return arrayOf(
                Item.get(ItemID.POTATO)
            )
        }

        var drops = 2
        val attempts = (3 + min(
            0.0,
            item.getEnchantmentLevel(Enchantment.ID_FORTUNE_DIGGING).toDouble()
        )).toInt()
        val random = ThreadLocalRandom.current()
        for (i in 0..<attempts) {
            if (random.nextInt(7) < 4) { // 4/7, 0.57142857142857142857142857142857
                drops++
            }
        }

        return if (random.nextInt(5) < 1) { // 1/5, 0.2
            arrayOf(
                get(ItemID.POTATO, 0, drops),
                Item.get(ItemID.POISONOUS_POTATO)
            )
        } else {
            arrayOf(
                get(ItemID.POTATO, 0, drops)
            )
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POTATOES, CommonBlockProperties.GROWTH)
    }
}
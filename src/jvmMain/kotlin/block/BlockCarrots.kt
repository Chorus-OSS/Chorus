package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class BlockCarrots @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCrops(blockstate) {
    override val name: String
        get() = "Carrot Block"

    override fun getDrops(item: Item): Array<Item> {
        if (!isFullyGrown) {
            return arrayOf(
                Item.get(ItemID.CARROT)
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

        return arrayOf(
            get(ItemID.CARROT, 0, drops)
        )
    }

    override fun toItem(): Item {
        return Item.get(ItemID.CARROT)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CARROTS, CommonBlockProperties.GROWTH)
    }
}
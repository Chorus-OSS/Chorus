package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

class BlockSeaLantern @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Sea Lantern"

    override val resistance: Double
        get() = 1.5

    override val hardness: Double
        get() = 0.3

    override val lightLevel: Int
        get() = 15

    override fun getDrops(item: Item): Array<Item> {
        val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
        val fortuneLevel = fortune?.level ?: 0
        // it drops 2–3 prismarine crystals
        // Each level of Fortune increases the maximum number of prismarine crystals dropped. 
        // The amount is capped at 5, so Fortune III simply increases the chance of getting 5 crystals.
        val count = min(5.0, (2 + ThreadLocalRandom.current().nextInt(1 + fortuneLevel)).toDouble()).toInt()

        return arrayOf(get(ItemID.PRISMARINE_CRYSTALS, 0, count))
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SEA_LANTERN)
    }
}

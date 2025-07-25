package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemGlowstoneDust
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.*

class BlockGlowstone(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Glowstone"

    override val resistance: Double
        get() = 1.5

    override val hardness: Double
        get() = 0.3

    override val lightLevel: Int
        get() = 15

    override fun getDrops(item: Item): Array<Item> {
        val random = Random()
        var count = 2 + random.nextInt(3)

        val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
        if (fortune != null && fortune.level >= 1) {
            count += random.nextInt(fortune.level + 1)
        }

        return arrayOf(
            ItemGlowstoneDust(0, count.coerceIn(1, 4))
        )
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GLOWSTONE)
    }
}

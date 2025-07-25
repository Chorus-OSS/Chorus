package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import java.util.concurrent.ThreadLocalRandom

class BlockTallGrass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoublePlant(blockstate) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.GRASS

    override val name: String
        get() = "Tallgrass"

    override val burnChance: Int
        get() = 60

    override val burnAbility: Int
        get() = 100

    override fun getDrops(item: Item): Array<Item> {
        // https://minecraft.wiki/w/Fortune#Grass_and_ferns
        val drops: MutableList<Item> = ArrayList(2)
        if (item.isShears) {
            drops.add(toItem())
        }

        val random = ThreadLocalRandom.current()
        if (random.nextInt(8) == 0) {
            val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
            val fortuneLevel = fortune?.level ?: 0
            val amount = if (fortuneLevel == 0) 1 else 1 + random.nextInt(fortuneLevel * 2)
            drops.add(get(ItemID.WHEAT_SEEDS, 0, amount))
        }

        return drops.toTypedArray()
    }

    override val toolType: Int
        get() = ItemTool.TYPE_SHEARS

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TALL_GRASS, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}

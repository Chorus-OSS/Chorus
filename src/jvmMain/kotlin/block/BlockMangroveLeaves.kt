package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class BlockMangroveLeaves(blockState: BlockState = properties.defaultState) : BlockLeaves(blockState) {
    override fun getType() = WoodType.MANGROVE

    override val name: String
        get() = "Mangrove Leaves"

    override fun getDrops(item: Item): Array<Item> {
        if (item.isShears) {
            return arrayOf(
                toItem()
            )
        }

        val drops: MutableList<Item> = ArrayList(1)
        val fortuneEnchantment = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)

        val fortune = fortuneEnchantment?.level ?: 0
        val stickOdds = when (fortune) {
            0 -> 50
            1 -> 45
            2 -> 40
            else -> 30
        }
        val random = ThreadLocalRandom.current()
        if (random.nextInt(stickOdds) == 0) {
            drops.add(Item.get(ItemID.STICK))
        }
        return drops.toTypedArray()
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        // TODO: 实现红树树叶催化
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.MANGROVE_LEAVES,
            CommonBlockProperties.PERSISTENT_BIT,
            CommonBlockProperties.UPDATE_BIT
        )
    }
}

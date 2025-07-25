package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace

open class BlockCandle @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    open fun toCakeForm(): Block {
        return BlockCandleCake()
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        var target1 = target
        if (target1!!.id == BlockID.CAKE && target1.isDefaultState) { //必须是完整的蛋糕才能插蜡烛
            target1.level.setBlock(target1.position, toCakeForm(), direct = true, update = true)
            return true
        }
        val up = target1.up()
        if (target1 !is BlockCandle && up is BlockCandle) {
            target1 = up
        }
        if (target1.id == id) {
            if (target1.getPropertyValue(CommonBlockProperties.CANDLES) < 3) {
                target1.setPropertyValue(
                    CommonBlockProperties.CANDLES, target1.getPropertyValue(
                        CommonBlockProperties.CANDLES
                    ) + 1
                )
                level.setBlock(target1.position, target1, direct = true, update = true)
                return true
            }
            return false
        } else if (target1 is BlockCandle) {
            return false
        }

        setPropertyValue(CommonBlockProperties.CANDLES, 0)
        level.setBlock(this.position, this, direct = true, update = true)

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
        if (item.id != ItemID.FLINT_AND_STEEL && !item.isNothing) {
            return false
        }

        if (getPropertyValue(CommonBlockProperties.LIT) && item.id != ItemID.FLINT_AND_STEEL) {
            setPropertyValue(CommonBlockProperties.LIT, false)
            level.addSound(this.position, Sound.RANDOM_FIZZ)
            level.setBlock(this.position, this, direct = true, update = true)
            return true
        } else if (!getPropertyValue(CommonBlockProperties.LIT) && item.id == ItemID.FLINT_AND_STEEL) {
            setPropertyValue(CommonBlockProperties.LIT, true)
            level.addSound(this.position, Sound.FIRE_IGNITE)
            level.setBlock(this.position, this, direct = true, update = true)
            return true
        }

        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            ItemBlock(properties.defaultState, name, 0, getPropertyValue(CommonBlockProperties.CANDLES) + 1)
        )
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val name: String
        get() = "Candle"

    override val lightLevel: Int
        get() = if (getPropertyValue(CommonBlockProperties.LIT)) getPropertyValue(
            CommonBlockProperties.CANDLES
        ) * 3 else 0

    override val hardness: Double
        get() = 0.1

    override val resistance: Double
        get() = 0.1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CANDLE, CommonBlockProperties.CANDLES, CommonBlockProperties.LIT)
    }
}
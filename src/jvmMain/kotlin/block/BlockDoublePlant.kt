package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags
import java.util.concurrent.ThreadLocalRandom

abstract class BlockDoublePlant(blockstate: BlockState) : BlockFlowable(blockstate) {
    abstract val doublePlantType: DoublePlantType

    var isTopHalf: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPPER_BLOCK_BIT)
        set(topHalf) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.UPPER_BLOCK_BIT,
                topHalf
            )
        }

    override fun toItem(): Item {
        val aux = doublePlantType.ordinal
        return ItemBlock(this.blockState, name, aux)
    }

    override val name: String
        /*@Override
            public boolean canBeReplaced() {
                return getDoublePlantType() == DoublePlantType.GRASS || getDoublePlantType() == DoublePlantType.FERN;
            }*/
        get() = doublePlantType.name

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isTopHalf) {
                // Top
                if (down() !is BlockDoublePlant) {
                    level.setBlock(this.position, get(BlockID.AIR), direct = false, update = true)
                    return Level.BLOCK_UPDATE_NORMAL
                }
            } else {
                // Bottom
                if (!isSupportValid(down())) {
                    level.setBlock(this.position, get(BlockID.AIR), direct = false, update = true)
                    return Level.BLOCK_UPDATE_NORMAL
                }
            }
        }
        return 0
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
        val up = up()

        if (up.isAir && isSupportValid(block.down())) {
            isTopHalf = false
            level.setBlock(
                block.position,
                this,
                direct = true,
                update = false
            ) // If we update the bottom half, it will drop the item because there isn't a flower block above

            isTopHalf = true
            level.setBlock(up.position, this, direct = true, update = true)
            level.updateAround(this.position)
            return true
        }

        return false
    }

    private fun isSupportValid(support: Block): Boolean {
        if (support is BlockDoublePlant) {
            return !support.isTopHalf
        }
        return support.`is`(BlockTags.DIRT)
    }

    override fun onBreak(item: Item?): Boolean {
        val down = down()

        if (isTopHalf) { // Top half
            level.useBreakOn(down.position)
        } else {
            level.setBlock(this.position, get(BlockID.AIR), direct = true, update = true)
        }

        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        if (isTopHalf) {
            return Item.EMPTY_ARRAY
        }

        if (doublePlantType == DoublePlantType.GRASS || doublePlantType == DoublePlantType.FERN) {
            val dropSeeds = ThreadLocalRandom.current().nextInt(10) == 0
            if (item.isShears) {
                //todo enchantment
                return if (dropSeeds) {
                    arrayOf(
                        Item.get(ItemID.WHEAT_SEEDS),
                        toItem()
                    )
                } else {
                    arrayOf(
                        toItem()
                    )
                }
            }
            return if (dropSeeds) {
                arrayOf(
                    Item.get(ItemID.WHEAT_SEEDS)
                )
            } else {
                Item.EMPTY_ARRAY
            }
        }

        return arrayOf(toItem())
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
        if (item.isFertilizer) { //Bone meal
            when (doublePlantType) {
                DoublePlantType.SUNFLOWER, DoublePlantType.SYRINGA, DoublePlantType.ROSE, DoublePlantType.PAEONIA -> {
                    if (player != null && (player.gamemode and 0x01) == 0) {
                        item.count--
                    }
                    level.addParticle(BoneMealParticle(this.position))
                    level.dropItem(this.position, toItem())
                }

                else -> Unit
            }

            return true
        }

        return false
    }

    override val isFertilizable: Boolean
        get() = true
}

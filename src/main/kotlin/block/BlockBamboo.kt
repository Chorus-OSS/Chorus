package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.BambooLeafSize
import org.chorus_oss.chorus.block.property.enums.BambooStalkThickness
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class BlockBamboo @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), FlowerPotBlock {
    override val name: String
        get() = "Bamboo"

    override fun onUpdate(type: Int): Int {
        when (type) {
            Level.BLOCK_UPDATE_NORMAL -> {
                if (isSupportInvalid) {
                    level.scheduleUpdate(this, 0)
                }
                return type
            }

            Level.BLOCK_UPDATE_SCHEDULED -> {
                level.useBreakOn(this.position, null, null, true)
            }

            Level.BLOCK_UPDATE_RANDOM -> {
                val up = up()
                if (age == 0 && up.isAir && level.getFullLight(up.position) >= BlockCrops.MIN_LIGHT_LEVEL && ThreadLocalRandom.current()
                        .nextInt(3) == 0
                ) {
                    grow(up)
                }
                return type
            }
        }
        return 0
    }

    fun grow(up: Block): Boolean {
        val newState = BlockBamboo()
        if (isThick) {
            newState.isThick = true
            newState.bambooLeafSize = BambooLeafSize.SMALL_LEAVES
        } else {
            newState.bambooLeafSize = BambooLeafSize.SMALL_LEAVES
        }
        val blockGrowEvent = BlockGrowEvent(up, newState)
        Server.instance.pluginManager.callEvent(blockGrowEvent)
        if (!blockGrowEvent.cancelled) {
            val newState1 = blockGrowEvent.newState
            newState1.position.x = position.x
            newState1.position.y = up.position.y
            newState1.position.z = position.z
            newState1.level = level
            newState1.place(toItem(), up, this, BlockFace.DOWN, 0.5, 0.5, 0.5, null)
            return true
        }
        return false
    }

    fun countHeight(): Int {
        var count = 0
        var opt: Optional<Block>
        var down: Block = this
        while ((down.down().firstInLayers { b: Block? -> b!!.id === BlockID.BAMBOO }.also { opt = it }).isPresent) {
            down = opt.get()
            if (++count >= 16) {
                break
            }
        }
        return count
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
        var down = down()
        val downId = down.id
        if (downId != BlockID.BAMBOO && downId != BlockID.BAMBOO_SAPLING) {
            val sampling = BlockBambooSapling()
            sampling.position.x = position.x
            sampling.position.y = position.y
            sampling.position.z = position.z
            sampling.level = level
            return sampling.place(item, block, target, face, fx, fy, fz, player)
        }

        var canGrow = true

        if (downId == BlockID.BAMBOO_SAPLING) {
            if (player != null) {
                val animatePacket = org.chorus_oss.protocol.packets.AnimatePacket(
                    targetRuntimeID = player.getRuntimeID().toULong(),
                    action = org.chorus_oss.protocol.packets.AnimatePacket.Action.SwingArm
                )
                level.addChunkPacket(player.position.chunkX, player.position.chunkZ, animatePacket)
            }
            bambooLeafSize = BambooLeafSize.SMALL_LEAVES
        }
        if (down is BlockBamboo) {
            canGrow = down.age == 0
            val thick = down.isThick
            if (!thick) {
                var setThick = true
                for (i in 2..3) {
                    if (getSide(BlockFace.DOWN, i).id !== BlockID.BAMBOO) {
                        setThick = false
                    }
                }
                if (setThick) {
                    isThick = true
                    bambooLeafSize = BambooLeafSize.LARGE_LEAVES
                    down.bambooLeafSize = BambooLeafSize.SMALL_LEAVES
                    down.isThick = true
                    down.age = 1
                    level.setBlock(down.position, down, direct = false, update = true)

                    down = down.down()
                    while (down is BlockBamboo) {
                        down.isThick = true
                        down.bambooLeafSize = BambooLeafSize.NO_LEAVES
                        down.age = 1
                        level.setBlock(down.position, down, direct = false, update = true)

                        down = down.down()
                    }
                } else {
                    bambooLeafSize = BambooLeafSize.SMALL_LEAVES
                    down.age = 1
                    level.setBlock(down.position, down, direct = false, update = true)
                }
            } else {
                isThick = true
                bambooLeafSize = BambooLeafSize.LARGE_LEAVES
                age = 0
                down.bambooLeafSize = BambooLeafSize.LARGE_LEAVES
                down.age = 1
                level.setBlock(down.position, down, direct = false, update = true)
                down = down.down()
                if (down is BlockBamboo) {
                    down.bambooLeafSize = BambooLeafSize.SMALL_LEAVES
                    down.age = 1
                    level.setBlock(down.position, down, direct = false, update = true)
                    down = down.down()
                    if (down is BlockBamboo) {
                        down.bambooLeafSize = BambooLeafSize.NO_LEAVES
                        down.age = 1
                        level.setBlock(down.position, down, direct = false, update = true)
                    }
                }
            }
        } else if (isSupportInvalid) {
            return false
        }

        val height = if (canGrow) countHeight() else 0
        if (!canGrow || height >= 15 || height >= 11 && ThreadLocalRandom.current().nextFloat() < 0.25f) {
            age = 1
        }

        level.setBlock(this.position, this, direct = false, update = true)
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        val down = down().firstInLayers { b: Block? -> b is BlockBamboo }
        if (down.isPresent) {
            val bambooDown = down.get() as BlockBamboo
            val height = bambooDown.countHeight()
            if (height < 15 && (height < 11 || !(ThreadLocalRandom.current().nextFloat() < 0.25f))) {
                bambooDown.age = 0
                level.setBlock(bambooDown.position, bambooDown.layer, bambooDown, direct = false, update = true)
            }
        }
        return super.onBreak(item)
    }

    override fun canPassThrough(): Boolean {
        return true
    }

    private val isSupportInvalid: Boolean
        get() = when (down().id) {
            BlockID.BAMBOO, BlockID.DIRT, BlockID.GRASS_BLOCK, BlockID.SAND, BlockID.GRAVEL, BlockID.PODZOL, BlockID.BAMBOO_SAPLING, BlockID.MOSS_BLOCK -> false
            else -> true
        }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 5.0

    var isThick: Boolean
        get() = bambooStalkThickness == BambooStalkThickness.THICK
        set(thick) {
            bambooStalkThickness = if (thick) BambooStalkThickness.THICK else BambooStalkThickness.THIN
        }

    var bambooStalkThickness: BambooStalkThickness
        get() = getPropertyValue(CommonBlockProperties.BAMBOO_STALK_THICKNESS)
        set(value) {
            setPropertyValue(
                CommonBlockProperties.BAMBOO_STALK_THICKNESS,
                value
            )
        }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    var bambooLeafSize: BambooLeafSize
        get() = getPropertyValue(CommonBlockProperties.BAMBOO_LEAF_SIZE)
        set(bambooLeafSize) {
            setPropertyValue(
                CommonBlockProperties.BAMBOO_LEAF_SIZE,
                bambooLeafSize
            )
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
        if (item.isFertilizer) {
            var top = position.y.toInt()
            var count = 1

            for (i in 1..16) {
                val id = level.getBlockIdAt(
                    position.floorX,
                    position.floorY - i, position.floorZ
                )
                if (id == BlockID.BAMBOO) {
                    count++
                } else {
                    break
                }
            }

            for (i in 1..16) {
                val id = level.getBlockIdAt(
                    position.floorX,
                    position.floorY + i, position.floorZ
                )
                if (id == BlockID.BAMBOO) {
                    top++
                    count++
                } else {
                    break
                }
            }

            //15格以上需要嫁接（放置竹子）
            if (count >= 15) {
                return false
            }

            var success = false

            val block = this.up(top - position.y.toInt() + 1)
            if (block.id === BlockID.AIR) {
                success = grow(block)
            }

            if (success) {
                if (player != null && player.isSurvival) {
                    item.count--
                }
                level.addParticle(BoneMealParticle(this.position))
            }

            return true
        }
        return false
    }

    var age: Int
        get() = if (getPropertyValue(CommonBlockProperties.AGE_BIT)) 1 else 0
        set(age) {
            var age1 = age
            age1 = age1.coerceIn(0, 1)
            setPropertyValue(CommonBlockProperties.AGE_BIT, age1 == 1)
        }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BAMBOO,
            CommonBlockProperties.AGE_BIT,
            CommonBlockProperties.BAMBOO_LEAF_SIZE,
            CommonBlockProperties.BAMBOO_STALK_THICKNESS
        )

    }
}

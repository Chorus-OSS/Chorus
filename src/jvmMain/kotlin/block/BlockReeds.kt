package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSugarCane
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags

class BlockReeds @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    override val name: String
        get() = "Reeds"

    override fun toItem(): Item {
        return ItemSugarCane()
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    var age: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_16)
        set(age) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_16, age)
        }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isFertilizer) { //Bonemeal
            var count = 1

            for (i in 1..2) {
                val id = level.getBlockIdAt(
                    position.floorX,
                    position.floorY - i, position.floorZ
                )

                if (id == BlockID.REEDS) {
                    count++
                }
            }

            if (count < 3) {
                var success = false
                val toGrow = 3 - count

                for (i in 1..toGrow) {
                    val block = this.up(i)
                    if (block.isAir) {
                        val ev = BlockGrowEvent(block, get(BlockID.REEDS))
                        instance.pluginManager.callEvent(ev)

                        if (!ev.cancelled) {
                            level.setBlock(block.position, ev.newState, true)
                            success = true
                        }
                    } else if (block.id != BlockID.REEDS) {
                        break
                    }
                }

                if (success) {
                    if (player != null && (player.gamemode and 0x01) == 0) {
                        item.count--
                    }

                    level.addParticle(BoneMealParticle(this.position))
                }
            }

            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        var level = level
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            level.scheduleUpdate(this, 0)
            return type
        }

        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!isSupportValid) {
                level.useBreakOn(this.position)
            }
            return type
        }

        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (!isSupportValid) {
                level.scheduleUpdate(this, 0)
                return type
            }
            if (age < 15) {
                age = age + 1
                level.setBlock(this.position, this, false)
                return type
            }
            val up = up()
            if (!up.isAir) {
                return type
            }

            var height = 0
            var current: Block? = this
            while (height < 3 && current!!.id == BlockID.REEDS) {
                current = current.down()
                height++
            }
            if (height >= 3) {
                return type
            }

            val ev: BlockGrowEvent = BlockGrowEvent(up, get(BlockID.REEDS))
            instance.pluginManager.callEvent(ev)

            if (ev.cancelled) {
                return type
            }

            if (!level.setBlock(up.position, get(BlockID.REEDS), false)) {
                return type
            }

            age = 0
            level.setBlock(this.position, this, false)
            return type
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
        if (!block.isAir) {
            return false
        }
        if (isSupportValid) {
            level.setBlock(block.position, this, true)
            return true
        }
        return false
    }

    private val isSupportValid: Boolean
        get() {
            val down = this.down()
            val downId = down.id
            if (downId == BlockID.REEDS) {
                return true
            }
            if (!down.`is`(BlockTags.DIRT) && !down.`is`(BlockTags.SAND)) {
                return false
            }
            for (face in BlockFace.Plane.HORIZONTAL_FACES) {
                val possibleWater = down.getSide(face)
                if (possibleWater is BlockFlowingWater
                    || possibleWater is BlockFrostedIce
                    || possibleWater.getLevelBlockAtLayer(1) is BlockFlowingWater
                ) {
                    return true
                }
            }
            return false
        }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.REEDS, CommonBlockProperties.AGE_16)
    }
}
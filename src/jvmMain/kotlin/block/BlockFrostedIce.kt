package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class BlockFrostedIce @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Frosted Ice"

    override val resistance: Double
        get() = 2.5

    override val hardness: Double
        get() = 0.5

    override val frictionFactor: Double
        get() = 0.98

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
        val success = super.place(item, block, target, face, fx, fy, fz, player)
        if (success) {
            level.scheduleUpdate(this, ThreadLocalRandom.current().nextInt(20, 40))
        }
        return success
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.FLOWING_WATER), true)
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (level.getBlockLightAt(floorX, floorY, floorZ) > 11 && (ThreadLocalRandom.current()
                    .nextInt(3) == 0 || countNeighbors() < 4)
            ) {
                slightlyMelt(true)
            } else {
                level.scheduleUpdate(this, ThreadLocalRandom.current().nextInt(20, 40))
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (countNeighbors() < 2) {
                level.setBlock(this.position, layer, get(BlockID.FLOWING_WATER), true)
            }
        }
        return super.onUpdate(type)
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    protected fun slightlyMelt(isSource: Boolean) {
        val age = age
        if (age < 3) {
            this.age = age + 1
            level.setBlock(this.position, layer, this, true)
            level.scheduleUpdate(level.getBlock(this.position), ThreadLocalRandom.current().nextInt(20, 40))
        } else {
            level.setBlock(this.position, layer, get(BlockID.FLOWING_WATER), true)
            if (isSource) {
                for (face in BlockFace.entries) {
                    val block = getSide(face)
                    if (block is BlockFrostedIce) {
                        block.slightlyMelt(false)
                    }
                }
            }
        }
    }

    private fun countNeighbors(): Int {
        var neighbors = 0
        for (face in BlockFace.entries) {
            if (getSide(face).id == BlockID.FROSTED_ICE && ++neighbors >= 4) {
                return neighbors
            }
        }
        return neighbors
    }

    var age: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_4)
        set(age) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_4, age)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FROSTED_ICE, CommonBlockProperties.AGE_4)
    }
}
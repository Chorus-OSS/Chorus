package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityEndPortal
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class BlockEndPortal @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState), BlockEntityHolder<BlockEntityEndPortal> {
    override val name: String
        get() = "End Portal Block"

    override fun getBlockEntityClass() = BlockEntityEndPortal::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.END_PORTAL
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
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return player != null && player.isCreative
    }

    override val hardness: Double
        get() = -1.0

    override val resistance: Double
        get() = 18000000.0

    override val lightLevel: Int
        get() = 15

    override val collisionBoundingBox: AxisAlignedBB
        get() = this

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override var maxY: Double
        get() = y + (12.0 / 16.0)
        set(maxY) {
            super.maxY = maxY
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.END_PORTAL)

        fun spawnObsidianPlatform(locator: Locator) {
            val level = locator.level
            val x = locator.position.floorX
            val y = locator.position.floorY
            val z = locator.position.floorZ

            for (blockX in x - 2..x + 2) {
                for (blockZ in z - 2..z + 2) {
                    level.setBlockStateAt(blockX, y - 1, blockZ, properties.defaultState)
                    for (blockY in y..y + 3) {
                        level.setBlockStateAt(blockX, blockY, blockZ, BlockAir.properties.defaultState)
                    }
                }
            }
        }
    }
}

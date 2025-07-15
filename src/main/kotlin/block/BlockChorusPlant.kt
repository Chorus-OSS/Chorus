package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class BlockChorusPlant(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Chorus Plant"

    override val hardness: Double
        get() = 0.4

    override val resistance: Double
        get() = 0.4

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    private val isPositionValid: Boolean
        get() {
            // (a chorus plant with at least one other chorus plant horizontally adjacent) breaks unless (at least one of the vertically adjacent blocks is air)
            // (a chorus plant) breaks unless (the block below is (chorus plant or end stone)) or (any horizontally adjacent block is a (chorus plant above (chorus plant or end stone_))
            var horizontal = false
            var horizontalSupported = false
            val down = down()
            for (face in BlockFace.Plane.HORIZONTAL_FACES) {
                val side = getSide(face)
                if (side.id == BlockID.CHORUS_PLANT) {
                    if (!horizontal) {
                        if (up().id != BlockID.AIR && down.id != BlockID.AIR) {
                            return false
                        }
                        horizontal = true
                    }

                    val sideSupport = side.down()
                    if (sideSupport.id == BlockID.CHORUS_PLANT || sideSupport.id == BlockID.END_STONE) {
                        horizontalSupported = true
                    }
                }
            }

            if (horizontal && horizontalSupported) {
                return true
            }

            return down.id == BlockID.CHORUS_PLANT || down.id == BlockID.END_STONE
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isPositionValid) {
                level.scheduleUpdate(this, 1)
                return type
            }
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            level.useBreakOn(this.position, null, null, true)
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
        if (!isPositionValid) {
            return false
        }
        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (ThreadLocalRandom.current().nextBoolean()) arrayOf(
            get(
                ItemID.CHORUS_FRUIT,
                0,
                1
            )
        ) else Item.EMPTY_ARRAY
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHORUS_PLANT)
    }
}

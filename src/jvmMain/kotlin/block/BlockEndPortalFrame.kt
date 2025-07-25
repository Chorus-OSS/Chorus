package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable

class BlockEndPortalFrame @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable {
    override val resistance: Double
        get() = 3600000.0

    override val hardness: Double
        get() = -1.0

    override val lightLevel: Int
        get() = 1

    override val waterloggingLevel: Int
        get() = 1

    override val name: String
        get() = "End Portal Frame"

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return player != null && player.isCreative
    }

    override var maxY: Double
        get() = position.y + (if (this.isEndPortalEye) 1.0 else 0.8125)
        set(maxY) {
            super.maxY = maxY
        }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() = if (this.isEndPortalEye) 15 else 0

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
        if (!this.isEndPortalEye && player != null && item.id == ItemID.ENDER_EYE) {
            this.isEndPortalEye = true
            level.setBlock(this.position, this, direct = true, update = true)
            level.addSound(this.position, Sound.BLOCK_END_PORTAL_FRAME_FILL)
            this.createPortal()
            return true
        }
        return false
    }

    fun createPortal() {
        val centerSpot = this.searchCenter(ArrayList())
        if (centerSpot != null) {
            for (x in -2..2) {
                for (z in -2..2) {
                    if ((x == -2 || x == 2) && (z == -2 || z == 2)) continue
                    if (x == -2 || x == 2 || z == -2 || z == 2) {
                        if (!this.checkFrame(
                                level.getBlock(centerSpot.add(x.toDouble(), 0.0, z.toDouble())),
                                x,
                                z
                            )
                        ) {
                            return
                        }
                    }
                }
            }

            for (x in -1..1) {
                for (z in -1..1) {
                    val vector3 = centerSpot.add(x.toDouble(), 0.0, z.toDouble())
                    if (!level.getBlock(vector3).isAir) {
                        level.useBreakOn(vector3)
                    }
                    level.setBlock(vector3, get(BlockID.END_PORTAL))
                }
            }
        }
    }

    private fun searchCenter(visited: MutableList<Block?>): Vector3? {
        for (x in -2..2) {
            if (x == 0) continue
            var block = level.getBlock(position.add(x.toDouble(), 0.0, 0.0))
            val iBlock = level.getBlock(position.add((x * 2).toDouble(), 0.0, 0.0))
            if (this.checkFrame(block) && !visited.contains(block)) {
                visited.add(block)
                if ((x == -1 || x == 1) && this.checkFrame(iBlock)) return (block as BlockEndPortalFrame).searchCenter(
                    visited
                )
                for (z in -4..4) {
                    if (z == 0) continue
                    block = level.getBlock(position.add(x.toDouble(), 0.0, z.toDouble()))
                    if (this.checkFrame(block)) {
                        return position.add(x.toDouble() / 2, 0.0, z.toDouble() / 2)
                    }
                }
            }
        }
        for (z in -2..2) {
            if (z == 0) continue
            var block = level.getBlock(position.add(0.0, 0.0, z.toDouble()))
            val iBlock = level.getBlock(position.add(0.0, 0.0, (z * 2).toDouble()))
            if (this.checkFrame(block) && !visited.contains(block)) {
                visited.add(block)
                if ((z == -1 || z == 1) && this.checkFrame(iBlock)) return (block as BlockEndPortalFrame).searchCenter(
                    visited
                )
                for (x in -4..4) {
                    if (x == 0) continue
                    block = level.getBlock(position.add(x.toDouble(), 0.0, z.toDouble()))
                    if (this.checkFrame(block)) {
                        return position.add(x.toDouble() / 2, 0.0, z.toDouble() / 2)
                    }
                }
            }
        }
        return null
    }

    private fun checkFrame(block: Block): Boolean {
        return block.id == this.id && (block as BlockEndPortalFrame).isEndPortalEye
    }

    private fun checkFrame(block: Block, x: Int, z: Int): Boolean {
        return block.id == this.id && (block.blockState.specialValue() - 4) == (if (x == -2) 3 else if (x == 2) 1 else if (z == -2) 0 else if (z == 2) 2 else -1)
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            this.setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
            )
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
        blockFace = if (player == null) {
            BlockFace.SOUTH
        } else {
            player.getDirection().getOpposite()
        }
        level.setBlock(block.position, this, true)
        return true
    }

    var isEndPortalEye: Boolean
        get() = getPropertyValue(CommonBlockProperties.END_PORTAL_EYE_BIT)
        set(endPortalEye) {
            setPropertyValue(
                CommonBlockProperties.END_PORTAL_EYE_BIT,
                endPortalEye
            )
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.END_PORTAL_FRAME,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.END_PORTAL_EYE_BIT
        )
    }
}

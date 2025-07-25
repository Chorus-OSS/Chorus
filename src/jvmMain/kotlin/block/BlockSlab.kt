package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.MinecraftVerticalHalf
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.registry.Registries


abstract class BlockSlab : BlockTransparent {
    protected val doubleSlab: BlockState?

    constructor(blockState: BlockState, doubleSlab: BlockState?) : super(blockState) {
        this.doubleSlab = doubleSlab
    }

    constructor(blockState: BlockState, doubleSlab: String) : super(blockState) {
        this.doubleSlab = Registries.BLOCK.get(doubleSlab)!!.blockState
    }

    abstract fun getSlabName(): String

    abstract override fun canHarvestWithHand(): Boolean

    abstract override val toolTier: Int

    abstract override val toolType: Int

    override val name: String
        get() = (if (isOnTop) "Upper " else "") + getSlabName() + " Slab"

    override var minY: Double
        get() = if (isOnTop) position.y + 0.5 else position.y
        set(minY) {
            super.minY = minY
        }

    override var maxY: Double
        get() = if (isOnTop) position.y + 1 else position.y + 0.5
        set(maxY) {
            super.maxY = maxY
        }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = (if (toolType == ItemTool.TYPE_PICKAXE) 6 else 3).toDouble()

    override val waterloggingLevel: Int
        get() = 1

    var isOnTop: Boolean
        get() = getPropertyValue(CommonBlockProperties.MINECRAFT_VERTICAL_HALF) == MinecraftVerticalHalf.TOP
        set(top) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                if (top) MinecraftVerticalHalf.TOP else MinecraftVerticalHalf.BOTTOM
            )
        }

    abstract fun isSameType(slab: BlockSlab): Boolean

    override fun isSolid(side: BlockFace): Boolean {
        return side == BlockFace.UP && isOnTop || side == BlockFace.DOWN && !isOnTop
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
        isOnTop = false
        if (face == BlockFace.DOWN) {
            if (target is BlockSlab && target.isOnTop && isSameType(target)) {
                level.setBlock(target.position, get(doubleSlab), true)

                return true
            } else if (block is BlockSlab && isSameType(block)) {
                level.setBlock(block.position, get(doubleSlab), true)

                return true
            } else {
                isOnTop = true
            }
        } else if (face == BlockFace.UP) {
            if (target is BlockSlab && !target.isOnTop && isSameType(target)) {
                level.setBlock(target.position, get(doubleSlab), true)

                return true
            } else if (block is BlockSlab && isSameType(block)) {
                level.setBlock(block.position, get(doubleSlab), true)

                return true
            }
            //TODO: check for collision
        } else {
            if (block is BlockSlab) {
                if (isSameType(block)) {
                    level.setBlock(block.position, get(doubleSlab), true)

                    return true
                }

                return false
            } else {
                if (fy > 0.5) {
                    isOnTop = true
                }
            }
        }

        if (block is BlockSlab && !isSameType(block)) {
            return false
        }
        level.setBlock(block.position, this, true, true)

        return true
    }
}

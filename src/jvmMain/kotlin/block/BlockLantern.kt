package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.ItemTool.Companion.getBestTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace

open class BlockLantern @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    override val name: String
        get() = "Lantern"

    private val isBlockAboveValid: Boolean
        get() {
            val support = up()
            when (support.id) {
                BlockID.CHAIN, BlockID.IRON_BARS, BlockID.HOPPER -> {
                    return true
                }

                else -> {
                    if (support is BlockWallBase || support is BlockFence) {
                        return true
                    }
                    if (support is BlockSlab && !support.isOnTop) {
                        return true
                    }
                    if (support is BlockStairs && !support.isUpsideDown) {
                        return true
                    }
                    return BlockLever.Companion.isSupportValid(support, BlockFace.DOWN)
                }
            }
        }

    private val isBlockUnderValid: Boolean
        get() {
            val support = down()
            if (support.id == BlockID.HOPPER) {
                return true
            }
            if (support is BlockWallBase || support is BlockFence) {
                return true
            }
            return BlockLever.Companion.isSupportValid(support, BlockFace.UP)
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
        val hanging = face != BlockFace.UP && isBlockAboveValid && (!isBlockUnderValid || face == BlockFace.DOWN)
        if (!isBlockUnderValid && !hanging) {
            return false
        }

        isHanging = hanging

        level.setBlock(this.position, this, true, true)
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isHanging) {
                if (!isBlockUnderValid) {
                    level.useBreakOn(this.position, getBestTool(toolType))
                }
            } else if (!isBlockAboveValid) {
                level.useBreakOn(this.position, getBestTool(toolType))
            }
            return type
        }
        return 0
    }

    override val lightLevel: Int
        get() = 15

    override val resistance: Double
        get() = 3.5

    override val hardness: Double
        get() = 3.5

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override var minX: Double
        get() = position.x + (5.0 / 16)
        set(minX) {
            super.minX = minX
        }

    override var minY: Double
        get() = position.y + (if (!isHanging) 0.0 else 1.0 / 16)
        set(minY) {
            super.minY = minY
        }

    override var minZ: Double
        get() = position.z + (5.0 / 16)
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + (11.0 / 16)
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + (if (!isHanging) 7.0 / 16 else 8.0 / 16)
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + (11.0 / 16)
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return this
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    var isHanging: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING)
        set(hanging) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, hanging)
        }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LANTERN, CommonBlockProperties.HANGING)
    }
}

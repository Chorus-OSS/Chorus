package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.utils.Faceable

class BlockLadder @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable {
    override val name: String
        get() = "Ladder"

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val hardness: Double
        get() = 0.4

    override val resistance: Double
        get() = 2.0

    private var offMinX = 0.0
    private var offMinZ = 0.0
    private var offMaxX = 0.0
    private var offMaxZ = 0.0

    private fun calculateOffsets() {
        val f = 0.1875
        when (this.blockFace) {
            BlockFace.NORTH -> {
                this.offMinX = 0.0
                this.offMinZ = 0.0
                this.offMaxX = 1.0
                this.offMaxZ = f
            }

            BlockFace.SOUTH -> {
                this.offMinX = 0.0
                this.offMinZ = 1 - f
                this.offMaxX = 1.0
                this.offMaxZ = 1.0
            }

            BlockFace.WEST -> {
                this.offMinX = 0.0
                this.offMinZ = 0.0
                this.offMaxX = f
                this.offMaxZ = 1.0
            }

            BlockFace.EAST -> {
                this.offMinX = 1 - f
                this.offMinZ = 0.0
                this.offMaxX = 1.0
                this.offMaxZ = 1.0
            }

            else -> {
                this.offMinX = 0.0
                this.offMinZ = 1.0
                this.offMaxX = 1.0
                this.offMaxZ = 1.0
            }
        }
    }

    override var minX: Double
        get() {
            calculateOffsets()
            return position.x + offMinX
        }
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() {
            calculateOffsets()
            return position.z + offMinZ
        }
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() {
            calculateOffsets()
            return position.x + offMaxX
        }
        set(maxX) {
            super.maxX = maxX
        }

    override var maxZ: Double
        get() {
            calculateOffsets()
            return position.z + offMaxZ
        }
        set(maxZ) {
            super.maxZ = maxZ
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
        if (target is BlockLadder) {
            val opposite = face.getOpposite()
            val oppositeB = level.getBlock(target.position.add(face.unitVector))
            val targetBlock = level.getBlock(target.position.add(face.unitVector.multiply(2.0)))
            if (isSupportValid(targetBlock, opposite)) {
                //不设置damage是因为level#useItemOn中有逻辑设置
                level.setBlock(oppositeB.position, this, true, false)
                return true
            }
        }
        if (face.horizontalIndex == -1 || !isSupportValid(target!!, face)) {
            return false
        }
        //不设置damage是因为level#useItemOn中有逻辑设置
        level.setBlock(block.position, this, true, true)
        return true
    }

    private fun isSupportValid(support: Block, face: BlockFace?): Boolean {
        if (support is BlockGlassStained || support is BlockBlackStainedGlassPane
            || support is BlockLeaves
        ) return false
        if (support.id == BlockID.BEACON) return false
        return BlockLever.Companion.isSupportValid(support, face!!)
    }

    override fun onUpdate(type: Int): Int {
        //debug
        /*for (double x = getMinX(); x <= getMaxX(); x += 0.2) {
            for (double y = getMinY(); y <= getMaxY(); y += 0.2) {
                for (double z = getMinZ(); z <= getMaxZ(); z += 0.2) {
                    level.addParticleEffect(new Vector3(x, y, z), ParticleEffect.ENDROD);
                }
            }
        }*/
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val face = blockFace
            if (!isSupportValid(getSide(face), face.getOpposite())) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        }
        return 0
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            get(BlockID.LADDER, 0, 1)
        )
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
            .getOpposite()
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
            calculateOffsets()
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
        val properties: BlockProperties = BlockProperties(BlockID.LADDER, CommonBlockProperties.FACING_DIRECTION)
    }
}

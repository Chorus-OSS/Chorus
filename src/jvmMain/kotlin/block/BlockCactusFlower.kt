package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockCactusFlower(blockState: BlockState = properties.defaultState) : BlockFlower(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

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
        if (!isBlockValidSupport(block.down())) {
            return false
        }
        return this.level.setBlock(this.position, this)
    }

    private fun isBlockValidSupport(block: Block): Boolean {
        val check = this.position.add(0.5, 0.0, 0.5)
        val box = block.boundingBox
        return (!block.isAir && box != null && box.isVectorInside(check)) || block is BlockCactus
    }

    override fun canPlantOn(block: Block): Boolean {
        return isBlockValidSupport(block)
    }

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CACTUS_FLOWER)
    }
}
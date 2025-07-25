package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockFireflyBush(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState) {
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
        if (BlockSweetBerryBush.isSupportValid(block.down())) {
            this.level.setBlock(block.position, this, true)
            return true
        }
        return false
    }

    override val lightLevel: Int
        get() = 2

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FIREFLY_BUSH)
    }
}
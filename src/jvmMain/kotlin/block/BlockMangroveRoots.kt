package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockMangroveRoots(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Mangrove Roots"

    override val waterloggingLevel: Int
        get() = 1

    override val hardness: Double
        get() = 0.7

    override val resistance: Double
        get() = 0.7

    override val burnChance: Int
        get() = 5

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
        return level.setBlock(this.position, this)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MANGROVE_ROOTS)
    }
}

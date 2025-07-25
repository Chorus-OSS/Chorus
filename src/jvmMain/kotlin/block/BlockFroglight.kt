package org.chorus_oss.chorus.block

abstract class BlockFroglight(blockState: BlockState) : BlockSolid(blockState) {
    override val lightLevel: Int
        get() = 15

    override val resistance: Double
        get() = 0.3
}

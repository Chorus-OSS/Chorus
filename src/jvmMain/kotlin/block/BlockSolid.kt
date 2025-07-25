package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.math.BlockFace


abstract class BlockSolid(blockState: BlockState) : Block(blockState) {
    override val isSolid: Boolean
        get() = true

    override fun isSolid(side: BlockFace): Boolean {
        return true
    }
}
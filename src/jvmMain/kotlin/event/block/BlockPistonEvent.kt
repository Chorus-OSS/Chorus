package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockPistonBase
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.math.BlockFace

class BlockPistonEvent(
    piston: BlockPistonBase,
    val direction: BlockFace,
    private val blocks: List<Block>,
    val destroyedBlocks: List<Block>,
    val isExtending: Boolean
) :
    BlockEvent(piston), Cancellable {
    fun getBlocks(): List<Block> {
        return ArrayList(blocks)
    }

    override val block: Block
        get() {
            return super.block as BlockPistonBase
        }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

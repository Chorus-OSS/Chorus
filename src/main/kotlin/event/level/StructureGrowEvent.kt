package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import java.util.*

class StructureGrowEvent(val block: Block, private val blocks: MutableList<Block>) :
    LevelEvent(block.level), Cancellable {
    var blockList: List<Block>
        get() = this.blocks
        set(blocks) {
            this.blocks.clear()
            this.blocks.addAll(blocks)
        }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

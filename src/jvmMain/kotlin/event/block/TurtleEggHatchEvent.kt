package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockTurtleEgg
import org.chorus_oss.chorus.block.property.enums.TurtleEggCount
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class TurtleEggHatchEvent(turtleEgg: BlockTurtleEgg, @JvmField var eggsHatching: Int, @JvmField var newState: Block) :
    BlockEvent(turtleEgg), Cancellable {
    var isRecalculateOnFailure: Boolean = true

    fun recalculateNewState() {
        var turtleEgg = block
        val eggCount = turtleEgg.eggCount.ordinal + 1
        val eggsHatching = this.eggsHatching
        if (eggCount <= eggsHatching) {
            newState = BlockAir()
        } else {
            turtleEgg = turtleEgg.clone()
            turtleEgg.eggCount = TurtleEggCount.entries[eggCount - eggsHatching - 1]
            newState = turtleEgg
        }
    }

    override val block: BlockTurtleEgg
        get() {
            return super.block as BlockTurtleEgg
        }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

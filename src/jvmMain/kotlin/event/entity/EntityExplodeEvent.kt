package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Locator

class EntityExplodeEvent(entity: Entity, locator: Locator, blocks: List<Block>, yield: Double) :
    EntityEvent(), Cancellable {
    val position: Locator
    var blockList: List<Block>

    @JvmField
    var ignitions: Set<Block>

    @JvmField
    var yield: Double

    init {
        this.entity = entity
        this.position = locator
        this.blockList = blocks
        this.yield = yield
        this.ignitions = HashSet(0)
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

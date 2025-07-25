package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class BlockExplosionPrimeEvent @JvmOverloads constructor(
    block: Block,
    val player: Player?,
    @JvmField var force: Double,
    @JvmField var fireChance: Double = 0.0
) :
    BlockEvent(block), Cancellable {
    var isBlockBreaking: Boolean = true

    constructor(block: Block, force: Double, fireChance: Double) : this(block, null, force, fireChance)

    var isIncendiary: Boolean
        get() = fireChance > 0
        set(incendiary) {
            if (!incendiary) {
                fireChance = 0.0
            } else if (fireChance <= 0) {
                fireChance = 1.0 / 3.0
            }
        }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

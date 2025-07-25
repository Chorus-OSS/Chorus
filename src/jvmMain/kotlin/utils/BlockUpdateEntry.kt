package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.atomic.AtomicLong


class BlockUpdateEntry : Comparable<BlockUpdateEntry> {
    var priority: Int = 0
    var delay: Long = 0


    var checkBlockWhenUpdate: Boolean = true

    val pos: Vector3
    val block: Block

    val id: Long

    constructor(pos: Vector3, block: Block) {
        this.pos = pos
        this.block = block
        this.id = entryID.getAndIncrement()
    }

    constructor(pos: Vector3, block: Block, delay: Long, priority: Int) {
        this.id = entryID.getAndIncrement()
        this.pos = pos
        this.priority = priority
        this.delay = delay
        this.block = block
    }

    constructor(pos: Vector3, block: Block, delay: Long, priority: Int, checkBlockWhenUpdate: Boolean) {
        this.id = entryID.getAndIncrement()
        this.pos = pos
        this.priority = priority
        this.delay = delay
        this.block = block
        this.checkBlockWhenUpdate = checkBlockWhenUpdate
    }

    override fun compareTo(other: BlockUpdateEntry): Int {
        return if (this.delay < other.delay) -1 else (if (this.delay > other.delay) 1 else (if (this.priority != other.priority) this.priority - other.priority else this.id.compareTo(
            other.id
        )))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockUpdateEntry) {
            if (other is Block) {
                return other.layer == block.layer && pos == other
            }
            if (other is Vector3) {
                return block.layer == 0 && pos == other
            }
            return false
        } else {
            return block.layer == other.block.layer && pos == other.pos && Block.equals(
                this.block,
                other.block,
                false
            )
        }
    }

    override fun hashCode(): Int {
        return pos.hashCode()
    }

    companion object {
        private val entryID = AtomicLong(0)
    }
}

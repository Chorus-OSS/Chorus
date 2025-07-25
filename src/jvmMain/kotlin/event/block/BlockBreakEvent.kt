package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace


class BlockBreakEvent(
    val player: Player?,
    block: Block,
    val face: BlockFace?,
    val item: Item?,
    drops: Array<Item>,
    instaBreak: Boolean,
    fastBreak: Boolean
) :
    BlockEvent(block), Cancellable {
    /**
     * 返回块是否可能在小于计算的时间内被破坏。通常创造玩家是true。
     *
     *
     * Returns whether the block may be broken in less than the amount of time calculated. This is usually true for creative players.
     *
     * @return the insta break
     */
    @JvmField
    var instaBreak: Boolean = false
    var drops: Array<Item> = Item.EMPTY_ARRAY
    var dropExp: Int = 0
    var isFastBreak: Boolean = false
        protected set

    @JvmOverloads
    constructor(
        player: Player?,
        block: Block,
        item: Item?,
        drops: Array<Item>,
        instaBreak: Boolean = false,
        fastBreak: Boolean = false
    ) : this(player, block, null, item, drops, instaBreak, fastBreak)

    init {
        this.instaBreak = instaBreak
        this.drops = drops
        this.isFastBreak = fastBreak
        this.dropExp = block.dropExp
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

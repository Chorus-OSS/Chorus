package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class PlayerBucketFillEvent(
    player: Player,
    blockClicked: Block,
    blockFace: BlockFace,
    liquid: Block,
    bucket: Item,
    itemInHand: Item
) :
    PlayerBucketEvent(player, blockClicked, blockFace, liquid, bucket, itemInHand) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

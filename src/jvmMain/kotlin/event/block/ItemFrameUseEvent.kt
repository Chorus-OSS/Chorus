package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.blockentity.BlockEntityItemFrame
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

/**
 * 物品展示框被使用的事件，会在放置物品，旋转物品，掉落物品时调用
 *
 *
 * The event that the item display box is used will be called when an item is placed, rotated, or dropped
 */
class ItemFrameUseEvent(
    /**
     * 获取使用物品展示框的玩家
     * Gets player.
     *
     * @return the player
     */
    val player: Player?, block: Block,
    /**
     * 获取被使用的物品展示框
     *
     *
     * Gets item frame.
     *
     * @return the item frame
     */
    val itemFrame: BlockEntityItemFrame,
    /**
     * 获取操作中的物品，例如放置物品到物品展示框，获取该物品
     *
     *
     * Get the item in action, e.g. place the item in the item display box and get the item
     *
     * @return the item
     */
    val item: Item?,
    /**
     * 获取操作类型，掉落，放置，选择
     *
     *
     * Get operation type, drop, place, select
     *
     * @return the action
     */
    val action: Action
) :
    BlockEvent(block), Cancellable {
    enum class Action {
        DROP,
        PUT,
        ROTATION
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

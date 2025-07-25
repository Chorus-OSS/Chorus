package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3


class PlayerInteractEvent @JvmOverloads constructor(
    player: Player,
    item: Item?,
    vector: IVector3,
    face: BlockFace?,
    action: Action = Action.RIGHT_CLICK_BLOCK
) :
    PlayerEvent(), Cancellable {
    var block: Block? = null
    val face: BlockFace?

    @JvmField
    val item: Item?
    var touchVector: Vector3? = null

    @JvmField
    val action: Action

    init {
        if (vector is Block) {
            this.block = vector
            this.touchVector = Vector3(0.0, 0.0, 0.0)
        } else {
            this.touchVector = vector.vector3
            this.block = Block.get(
                BlockID.AIR, Locator(
                    0.0, 0.0, 0.0,
                    player.level!!
                )
            )
        }

        this.player = player
        this.item = item
        this.face = face
        this.action = action
    }

    enum class Action {
        LEFT_CLICK_BLOCK,
        RIGHT_CLICK_BLOCK,
        LEFT_CLICK_AIR,
        RIGHT_CLICK_AIR,
        PHYSICAL
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

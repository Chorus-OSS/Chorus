package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.block.BlockFromToEvent
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.types.Vector3f
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class BlockDragonEgg(blockState: BlockState = properties.defaultState) : BlockFallable(blockState) {
    override val name: String
        get() = "Dragon Egg"

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 45.0

    override val lightLevel: Int
        get() = 1

    override val isTransparent: Boolean
        get() = true

    override val waterloggingLevel: Int
        get() = 1

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_TOUCH) {
            this.teleport()
        }
        return super.onUpdate(type)
    }

    override fun onTouch(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        action: PlayerInteractEvent.Action
    ) {
        if (player != null && (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
            if (player.isCreative && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                return
            }
            onUpdate(Level.BLOCK_UPDATE_TOUCH)
        }
    }

    fun teleport() {
        val random = ThreadLocalRandom.current()
        for (i in 0..999) {
            var to =
                level.getBlock(
                    position.add(
                        random.nextInt(-16, 16).toDouble(),
                        random.nextInt(0, 16).toDouble(),
                        random.nextInt(-16, 16).toDouble()
                    )
                )
            if (to.isAir) {
                val event = BlockFromToEvent(this, to)
                Server.instance.pluginManager.callEvent(event)
                if (event.cancelled) return
                to = event.to

                val diffX = position.floorX - to.position.floorX
                val diffY = position.floorY - to.position.floorY
                val diffZ = position.floorZ - to.position.floorZ
                val pk = LevelEventPacket(
                    eventType = LevelEventPacket.PARTICLE_DRAGON_EGG,
                    position = Vector3f(position.floor()),
                    eventData = (((((abs(diffX.toDouble()).toInt() shl 16) or (abs(diffY.toDouble()).toInt() shl 8)) or abs(diffZ.toDouble()).toInt()) or ((if (diffX < 0) 1 else 0) shl 24)) or ((if (diffY < 0) 1 else 0) shl 25)) or ((if (diffZ < 0) 1 else 0) shl 26)
                )
                level.addChunkPacket(
                    position.floorX shr 4,
                    position.floorZ shr 4, pk
                )
                level.setBlock(this.position, get(BlockID.AIR), true)
                level.setBlock(to.position, this, true)
                return
            }
        }
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DRAGON_EGG)
    }
}

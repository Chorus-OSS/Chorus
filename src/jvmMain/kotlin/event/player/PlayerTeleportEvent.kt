package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.math.Vector3

class PlayerTeleportEvent private constructor(player: Player) : PlayerEvent(), Cancellable {
    var cause: TeleportCause? = null
        private set
    var from: Transform? = null
        private set
    var to: Transform? = null
        private set

    init {
        this.player = player
    }

    constructor(player: Player, from: Transform?, to: Transform?, cause: TeleportCause?) : this(player) {
        this.from = from
        this.to = to
        this.cause = cause
    }

    constructor(player: Player, from: Vector3, to: Vector3, cause: TeleportCause?) : this(player) {
        this.from = vectorToLocation(player.level!!, from)
        this.from = vectorToLocation(player.level!!, to)
        this.cause = cause
    }

    private fun vectorToLocation(baseLevel: Level, vector: Vector3): Transform {
//        if (vector instanceof Transform) return (Transform) vector;
//        if (vector instanceof Locator) return ((Locator) vector).getLocation();
        return Transform(vector.x, vector.y, vector.z, 0.0, 0.0, baseLevel)
    }

    enum class TeleportCause {
        COMMAND,  // For Nukkit tp command only
        PLUGIN,  // Every plugin
        NETHER_PORTAL,  // Teleport using Nether portal
        ENDER_PEARL,  // Teleport by ender pearl
        CHORUS_FRUIT,  // Teleport by chorus fruit
        UNKNOWN,  // Unknown cause
        END_PORTAL,  // Teleport using End Portal
        SHULKER,
        END_GATEWAY,  // Teleport using End Gateway
        PLAYER_SPAWN // Teleport when players are spawn
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

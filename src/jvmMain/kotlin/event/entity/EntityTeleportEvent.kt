package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent.TeleportCause
import org.chorus_oss.chorus.level.Transform

class EntityTeleportEvent @JvmOverloads constructor(
    entity: Entity,
    from: Transform,
    to: Transform,
    cause: TeleportCause = TeleportCause.UNKNOWN
) :
    EntityEvent(), Cancellable {
    var from: Transform
    var to: Transform
    val cause: TeleportCause

    init {
        this.entity = entity
        this.from = from
        this.to = to
        this.cause = cause
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityCreature
import org.chorus_oss.chorus.entity.EntityHuman
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Locator


class EntitySpawnEvent(entity: Entity) : EntityEvent(), Cancellable {
    val type: Int

    init {
        this.entity = entity
        this.type = entity.getNetworkID()
    }

    val position: Locator
        get() = entity.locator

    val isCreature: Boolean
        get() = entity is EntityCreature

    val isHuman: Boolean
        get() = entity is EntityHuman

    val isProjectile: Boolean
        get() = entity is EntityProjectile

    val isVehicle: Boolean
        get() = entity is Entity

    val isItem: Boolean
        get() = entity is EntityItem

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

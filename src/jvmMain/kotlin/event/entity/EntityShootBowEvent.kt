package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class EntityShootBowEvent(shooter: EntityLiving, bow: Item, projectile: EntityProjectile, force: Double) :
    EntityEvent(), Cancellable {
    val bow: Item

    var projectile: EntityProjectile = projectile
        set(value) {
            if (value !== field) {
                if (field.viewers.isEmpty()) {
                    field.kill()
                    field.close()
                }
                field = projectile
            }
        }

    @JvmField
    var force: Double

    init {
        this.entity = shooter
        this.bow = bow
        this.projectile = projectile
        this.force = force
    }

    fun setProjectile(projectile: Entity) {
        if (projectile !== this.projectile) {
            if (this.projectile.viewers.isEmpty()) {
                this.projectile.kill()
                this.projectile.close()
            }
            this.projectile = projectile as EntityProjectile
        }
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

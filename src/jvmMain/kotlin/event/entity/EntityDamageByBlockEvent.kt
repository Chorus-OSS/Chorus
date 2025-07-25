package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity


class EntityDamageByBlockEvent(@JvmField val damager: Block, entity: Entity, cause: DamageCause, damage: Float) :
    EntityDamageEvent(entity, cause, damage)

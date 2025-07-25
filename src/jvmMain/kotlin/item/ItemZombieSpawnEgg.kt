package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.registry.Registries

class ItemZombieSpawnEgg : ItemSpawnEgg(ItemID.Companion.ZOMBIE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = Registries.ENTITY.getEntityNetworkId(EntityID.ZOMBIE)

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
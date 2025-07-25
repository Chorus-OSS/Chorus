package org.chorus_oss.chorus.item

class ItemZombieVillagerSpawnEgg : ItemSpawnEgg(ItemID.Companion.ZOMBIE_VILLAGER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 44

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
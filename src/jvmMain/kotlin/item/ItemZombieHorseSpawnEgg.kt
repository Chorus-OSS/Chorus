package org.chorus_oss.chorus.item

class ItemZombieHorseSpawnEgg : ItemSpawnEgg(ItemID.ZOMBIE_HORSE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 27

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
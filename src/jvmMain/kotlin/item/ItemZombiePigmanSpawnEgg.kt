package org.chorus_oss.chorus.item

class ItemZombiePigmanSpawnEgg : ItemSpawnEgg(ItemID.Companion.ZOMBIE_PIGMAN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 36

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
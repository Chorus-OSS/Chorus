package org.chorus_oss.chorus.item

class ItemHuskSpawnEgg : ItemSpawnEgg(ItemID.Companion.HUSK_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 47

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
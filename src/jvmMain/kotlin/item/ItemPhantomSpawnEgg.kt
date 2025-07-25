package org.chorus_oss.chorus.item

class ItemPhantomSpawnEgg : ItemSpawnEgg(ItemID.Companion.PHANTOM_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 58

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
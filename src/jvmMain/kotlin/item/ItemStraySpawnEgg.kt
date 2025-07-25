package org.chorus_oss.chorus.item

class ItemStraySpawnEgg : ItemSpawnEgg(ItemID.Companion.STRAY_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 46

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
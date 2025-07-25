package org.chorus_oss.chorus.item

class ItemWardenSpawnEgg : ItemSpawnEgg(ItemID.Companion.WARDEN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 131

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
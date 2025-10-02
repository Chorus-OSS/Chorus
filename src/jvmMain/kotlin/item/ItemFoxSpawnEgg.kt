package org.chorus_oss.chorus.item

class ItemFoxSpawnEgg : ItemSpawnEgg(ItemID.FOX_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 121

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
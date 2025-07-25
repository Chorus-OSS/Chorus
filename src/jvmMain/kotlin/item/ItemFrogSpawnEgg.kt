package org.chorus_oss.chorus.item

class ItemFrogSpawnEgg : ItemSpawnEgg(ItemID.Companion.FROG_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 132

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
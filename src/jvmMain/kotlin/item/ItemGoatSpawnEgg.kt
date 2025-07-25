package org.chorus_oss.chorus.item

class ItemGoatSpawnEgg : ItemSpawnEgg(ItemID.Companion.GOAT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 128

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
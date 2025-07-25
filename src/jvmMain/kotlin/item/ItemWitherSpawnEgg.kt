package org.chorus_oss.chorus.item

class ItemWitherSpawnEgg : ItemSpawnEgg(ItemID.Companion.WITHER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 52

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
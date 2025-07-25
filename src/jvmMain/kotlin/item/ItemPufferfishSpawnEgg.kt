package org.chorus_oss.chorus.item

class ItemPufferfishSpawnEgg : ItemSpawnEgg(ItemID.Companion.PUFFERFISH_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 108

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
package org.chorus_oss.chorus.item

class ItemPolarBearSpawnEgg : ItemSpawnEgg(ItemID.Companion.POLAR_BEAR_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 28

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
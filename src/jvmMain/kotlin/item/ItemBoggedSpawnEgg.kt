package org.chorus_oss.chorus.item

class ItemBoggedSpawnEgg : ItemSpawnEgg(ItemID.Companion.BOGGED_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 144

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
package org.chorus_oss.chorus.item

class ItemMuleSpawnEgg : ItemSpawnEgg(ItemID.Companion.MULE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 25

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
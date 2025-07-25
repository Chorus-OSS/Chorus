package org.chorus_oss.chorus.item

class ItemCowSpawnEgg : ItemSpawnEgg(ItemID.Companion.COW_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 11

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
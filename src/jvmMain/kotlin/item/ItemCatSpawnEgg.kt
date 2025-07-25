package org.chorus_oss.chorus.item

class ItemCatSpawnEgg : ItemSpawnEgg(ItemID.Companion.CAT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 75

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
package org.chorus_oss.chorus.item

class ItemCreakingSpawnEgg : ItemSpawnEgg(ItemID.Companion.CREAKING_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 146

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
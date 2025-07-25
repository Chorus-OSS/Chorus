package org.chorus_oss.chorus.item

class ItemVindicatorSpawnEgg : ItemSpawnEgg(ItemID.Companion.VINDICATOR_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 57

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
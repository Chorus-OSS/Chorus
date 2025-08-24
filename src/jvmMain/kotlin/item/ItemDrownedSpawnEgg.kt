package org.chorus_oss.chorus.item

class ItemDrownedSpawnEgg : ItemSpawnEgg(ItemID.DROWNED_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 110

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
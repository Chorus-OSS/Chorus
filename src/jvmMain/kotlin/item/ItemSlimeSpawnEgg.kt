package org.chorus_oss.chorus.item

class ItemSlimeSpawnEgg : ItemSpawnEgg(ItemID.Companion.SLIME_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 37

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
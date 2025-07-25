package org.chorus_oss.chorus.item

class ItemMooshroomSpawnEgg : ItemSpawnEgg(ItemID.Companion.MOOSHROOM_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 16

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
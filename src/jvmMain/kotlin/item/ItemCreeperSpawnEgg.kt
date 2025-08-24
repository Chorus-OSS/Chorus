package org.chorus_oss.chorus.item

class ItemCreeperSpawnEgg : ItemSpawnEgg(ItemID.CREEPER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 33

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
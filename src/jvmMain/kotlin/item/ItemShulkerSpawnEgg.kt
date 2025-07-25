package org.chorus_oss.chorus.item

class ItemShulkerSpawnEgg : ItemSpawnEgg(ItemID.Companion.SHULKER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 54

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
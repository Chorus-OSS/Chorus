package org.chorus_oss.chorus.item

class ItemBeeSpawnEgg : ItemSpawnEgg(ItemID.Companion.BEE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 122

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
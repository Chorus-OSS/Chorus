package org.chorus_oss.chorus.item

class ItemBreezeSpawnEgg : ItemSpawnEgg(ItemID.Companion.BREEZE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 140

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
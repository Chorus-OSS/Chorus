package org.chorus_oss.chorus.item

class ItemBlazeSpawnEgg : ItemSpawnEgg(ItemID.Companion.BLAZE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 43

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
package org.chorus_oss.chorus.item

class ItemHoglinSpawnEgg : ItemSpawnEgg(ItemID.Companion.HOGLIN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 124

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
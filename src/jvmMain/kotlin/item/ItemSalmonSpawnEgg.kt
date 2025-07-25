package org.chorus_oss.chorus.item

class ItemSalmonSpawnEgg : ItemSpawnEgg(ItemID.Companion.SALMON_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 109

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
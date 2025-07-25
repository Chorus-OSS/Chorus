package org.chorus_oss.chorus.item

class ItemParrotSpawnEgg : ItemSpawnEgg(ItemID.Companion.PARROT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 30

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
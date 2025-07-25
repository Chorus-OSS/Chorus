package org.chorus_oss.chorus.item

class ItemSquidSpawnEgg : ItemSpawnEgg(ItemID.Companion.SQUID_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 17

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}
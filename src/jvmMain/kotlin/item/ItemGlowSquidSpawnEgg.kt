package org.chorus_oss.chorus.item

class ItemGlowSquidSpawnEgg : ItemSpawnEgg(ItemID.Companion.GLOW_SQUID_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 129

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}